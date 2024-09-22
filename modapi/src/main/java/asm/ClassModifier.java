package asm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * A utility to add static fields and methods from source classes into target classes within a JAR.
 * <p>
 * Usage:
 * java -jar class-modifier.jar <input-jar> <output-jar> <replacements-dir> <class1> <class2> ... <classN>
 * <p>
 * Example:
 * java -jar class-modifier.jar original.jar modified.jar replacements com.example.MyClass com.example.utils.Helper
 */
public class ClassModifier
{

    /**
     * Main method to execute the class modification.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            System.err.println("Usage: java -jar class-modifier.jar <input-jar> <output-jar> <replacements-dir>");
            System.exit(1);
        }

        String inputJarPath = args[0];
        String outputJarPath = args[1];
        String replacementsDirPath = args[2];
        List<String> classesToModify = FileUtils.listFiles(
                new File(replacementsDirPath),
                new RegexFileFilter("^(.*?)"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(f -> f.getPath().substring(30).replace(".class", ""))
                .filter(p -> !p.contains("DS_Store")).toList();

        try
        {
            modifyClassesInJar(inputJarPath, outputJarPath, replacementsDirPath, classesToModify);
            System.out.println("Classes modified successfully. Modified JAR created at: " + outputJarPath);
        }
        catch (IOException e)
        {
            System.err.println("An error occurred during class modification:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Modifies specified classes in the input JAR by adding static fields and methods from replacement classes.
     *
     * @param inputJarPath        Path to the original JAR file.
     * @param outputJarPath       Path where the modified JAR will be saved.
     * @param replacementsDirPath Path to the directory containing replacement class files.
     * @param classesToModify     List of fully qualified class names to modify.
     * @throws IOException If an I/O error occurs.
     */
    public static void modifyClassesInJar(String inputJarPath, String outputJarPath,
                                          String replacementsDirPath, List<String> classesToModify) throws IOException
    {
        // Convert class names to JAR entry paths
        Set<String> classEntryPaths = new HashSet<>();
        for (String className : classesToModify)
        {
            String entryPath = className.replace('.', '/') + ".class";
            classEntryPaths.add(entryPath);
        }

        // Prepare to read the replacement class files
        Path replacementsDir = Paths.get(replacementsDirPath);
        if (!Files.isDirectory(replacementsDir))
        {
            throw new IOException("Replacements directory does not exist or is not a directory: " + replacementsDirPath);
        }

        // Open the input JAR
        try (JarFile inputJar = new JarFile(inputJarPath);
             JarOutputStream outputJar = new JarOutputStream(new FileOutputStream(outputJarPath)))
        {

            Enumeration<JarEntry> entries = inputJar.entries();
            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // Create a new entry for the output JAR
                JarEntry newEntry = new JarEntry(entryName);
                outputJar.putNextEntry(newEntry);

                if (classEntryPaths.contains(entryName))
                {
                    // Modify this class by adding static fields and methods
                    String classFilePath = replacementsDirPath + File.separator + entryName;
                    Path replacementClassPath = Paths.get(classFilePath);
                    if (!Files.exists(replacementClassPath))
                        throw new FileNotFoundException("Replacement class file not found: " + classFilePath);

                    byte[] replacementClassBytes = Files.readAllBytes(replacementClassPath);
                    // Extract static fields and methods from the replacement class
                    StaticMembersExtractor extractor = new StaticMembersExtractor();
                    ClassReader replacementClassReader = new ClassReader(replacementClassBytes);
                    replacementClassReader.accept(extractor, ClassReader.SKIP_FRAMES);

                    // Read the original class from the JAR
                    byte[] originalClassBytes;
                    try (InputStream is = inputJar.getInputStream(entry))
                    {
                        originalClassBytes = is.readAllBytes();
                    }

                    // Modify the original class by adding the extracted static fields and methods
                    byte[] modifiedClassBytes = addStaticMembersToClass(originalClassBytes, extractor.getFields(), extractor.getMethods());

                    // Write the modified class bytes to the output JAR
                    outputJar.write(modifiedClassBytes);
                    System.out.println("Modified class: " + entryName);
                }
                else
                {
                    // Copy the original entry bytes to the output JAR
                    try (InputStream is = inputJar.getInputStream(entry))
                    {
                        copyStream(is, outputJar);
                    }
                }

                outputJar.closeEntry();
            }

            // Handle classes to modify that were not present in the original JAR by adding them as new classes
            for (String entryPath : classEntryPaths)
            {
                if (inputJar.getEntry(entryPath) == null)
                {
                    // Add the replacement class to the output JAR
                    String classFilePath = replacementsDirPath + File.separator + entryPath;
                    Path replacementClassPath = Paths.get(classFilePath);
                    if (!Files.exists(replacementClassPath))
                        throw new FileNotFoundException("Replacement class file not found: " + classFilePath);

                    byte[] replacementClassBytes = Files.readAllBytes(replacementClassPath);
                    // Extract static fields and methods from the replacement class
                    StaticMembersExtractor extractor = new StaticMembersExtractor();
                    ClassReader replacementClassReader = new ClassReader(replacementClassBytes);
                    replacementClassReader.accept(extractor, ClassReader.SKIP_FRAMES);

                    // Since the class does not exist in the original JAR, create a new class with the static members
                    byte[] newClassBytes = createNewClassWithStaticMembers(entryPath, extractor.getFields(), extractor.getMethods());

                    JarEntry newClassEntry = new JarEntry(entryPath);
                    outputJar.putNextEntry(newClassEntry);
                    outputJar.write(newClassBytes);
                    outputJar.closeEntry();

                    System.out.println("Added new class to JAR: " + entryPath);
                }
            }
        }
    }

    /**
     * Adds static fields and methods to the target class bytecode.
     *
     * @param originalClassBytes Original class bytecode.
     * @param fieldsToAdd        List of static fields to add.
     * @param methodsToAdd       List of static methods to add.
     * @return Modified class bytecode with added static fields and methods.
     */
    private static byte[] addStaticMembersToClass(byte[] originalClassBytes, List<FieldNode> fieldsToAdd, List<MethodNode> methodsToAdd)
    {
        ClassReader classReader = new ClassReader(originalClassBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        // Use ASM's ClassVisitor to add static members without altering existing ones
        ClassVisitor classVisitor = new AddStaticMembersClassVisitor(Opcodes.ASM9, classWriter, fieldsToAdd, methodsToAdd);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

        return classWriter.toByteArray();
    }

    /**
     * Creates a new class bytecode with the specified static fields and methods.
     *
     * @param classEntryPath Path of the class within the JAR (e.g., com/example/MyClass.class).
     * @param fieldsToAdd    List of static fields to add.
     * @param methodsToAdd   List of static methods to add.
     * @return Bytecode of the newly created class.
     */
    private static byte[] createNewClassWithStaticMembers(String classEntryPath, List<FieldNode> fieldsToAdd, List<MethodNode> methodsToAdd)
    {
        String className = classEntryPath.substring(0, classEntryPath.length() - 6); // Remove ".class"
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        // Define the class as public and with the same name
        classWriter.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);

        // Add a default constructor
        MethodVisitor constructor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        // Load 'this'
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        // Invoke super constructor
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();

        // Add static fields
        for (FieldNode field : fieldsToAdd)
        {
            FieldVisitor fv = classWriter.visitField(field.access, field.name, field.desc, field.signature, field.value);
            if (fv != null)
            {
                field.accept(classWriter); // Correctly accept FieldVisitor
                fv.visitEnd();
            }
        }

        // Add static methods
        for (MethodNode method : methodsToAdd)
        {
            MethodVisitor mv = classWriter.visitMethod(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0]));
            if (mv != null)
            {
                method.accept(mv); // Correctly accept MethodVisitor
                mv.visitEnd();
            }
        }

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    /**
     * Copies all bytes from an InputStream to an OutputStream.
     *
     * @param is InputStream to read from.
     * @param os OutputStream to write to.
     * @throws IOException If an I/O error occurs.
     */
    private static void copyStream(InputStream is, OutputStream os) throws IOException
    {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1)
        {
            os.write(buffer, 0, bytesRead);
        }
    }

    /**
     * A ClassVisitor that adds static fields and methods to a class.
     */
    private static class AddStaticMembersClassVisitor extends ClassVisitor
    {
        private final List<FieldNode> fieldsToAdd;
        private final List<MethodNode> methodsToAdd;
        private final Set<String> existingFields = new HashSet<>();
        private final Set<String> existingMethods = new HashSet<>();

        public AddStaticMembersClassVisitor(int api, ClassVisitor classVisitor,
                                            List<FieldNode> fieldsToAdd, List<MethodNode> methodsToAdd)
        {
            super(api, classVisitor);
            this.fieldsToAdd = fieldsToAdd;
            this.methodsToAdd = methodsToAdd;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
        {
            existingFields.add(name);
            return super.visitField(access, name, descriptor, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
        {
            String methodKey = name + descriptor;
            existingMethods.add(methodKey);
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        @Override
        public void visitEnd()
        {
            // Add static fields if they don't already exist
            for (FieldNode field : fieldsToAdd)
            {
                if (!existingFields.contains(field.name))
                {
                    FieldVisitor fv = cv.visitField(field.access, field.name, field.desc, field.signature, field.value);
                    if (fv != null)
                    {
                        field.accept(cv); // Correctly accept FieldVisitor
                        fv.visitEnd();
                    }
                    System.out.println("Added static field: " + field.name);
                }
                else
                {
                    System.out.println("Field already exists, skipping: " + field.name);
                }
            }

            // Add static methods if they don't already exist
            for (MethodNode method : methodsToAdd)
            {
                String methodKey = method.name + method.desc;
                if (!existingMethods.contains(methodKey))
                {
                    MethodVisitor mv = cv.visitMethod(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0]));
                    if (mv != null)
                    {
                        method.accept(mv); // Correctly accept MethodVisitor
                        mv.visitEnd();
                    }
                    System.out.println("Added static method: " + method.name);
                }
                else
                {
                    System.out.println("Method already exists, skipping: " + method.name);
                }
            }

            super.visitEnd();
        }
    }

    /**
     * A ClassVisitor that extracts static fields and methods from a class.
     */
    private static class StaticMembersExtractor extends ClassVisitor
    {
        private final List<FieldNode> staticFields = new ArrayList<>();
        private final List<MethodNode> staticMethods = new ArrayList<>();

        public StaticMembersExtractor()
        {
            super(Opcodes.ASM9);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
        {
            if ((access & Opcodes.ACC_STATIC) != 0)
            {
                FieldNode fieldNode = new FieldNode(access, name, descriptor, signature, value);
                staticFields.add(fieldNode);
            }
            return super.visitField(access, name, descriptor, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
        {
            if ((access & Opcodes.ACC_STATIC) != 0 && !name.equals("<clinit>"))
            { // Exclude static initializer
                MethodNode methodNode = new MethodNode(access, name, descriptor, signature, exceptions);
                staticMethods.add(methodNode);
                return methodNode;
            }
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        public List<FieldNode> getFields()
        {
            return staticFields;
        }

        public List<MethodNode> getMethods()
        {
            return staticMethods;
        }
    }
}
