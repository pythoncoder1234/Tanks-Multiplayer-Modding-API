package asm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * A utility to replace classes within a JAR file using ASM.
 * <p>
 * Usage:
 * java -jar class-replacer.jar <input-jar> <output-jar> <replacements-dir> <class1> <class2> ... <classN>
 * <p>
 * Example:
 * java -jar class-replacer.jar original.jar modified.jar replacements com.example.MyClass com.example.utils.Helper
 */
public class ClassReplacer
{

    /**
     * Main method to execute the class replacement.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        if (args.length < 4)
        {
            System.err.println("Usage: java -jar class-replacer.jar <input-jar> <output-jar> <replacements-dir> <resources-dir>");
            System.exit(1);
        }

        String inputJarPath = args[0];
        String outputJarPath = args[1];
        String replacementsDirPath = args[2];
        String resourcesDirPath = args[3];
        List<String> classesToReplace = FileUtils.listFiles(
                        new File(replacementsDirPath),
                        new RegexFileFilter("^(.*?)"),
                        DirectoryFileFilter.DIRECTORY
                ).stream().map(f -> f.getPath().substring(30).replace(".class", ""))
                .filter(p -> !p.contains("DS_Store")).toList();

        try
        {
            replaceStuffInJar(inputJarPath, outputJarPath, replacementsDirPath, resourcesDirPath, classesToReplace);
            System.out.println("Classes replaced and resources added successfully. Modified JAR created at: " + outputJarPath);
        }
        catch (IOException e)
        {
            System.err.println("An error occurred during class replacement and resource addition:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Replaces specified classes in the input JAR and adds new resources.
     *
     * @param inputJarPath        Path to the original JAR file.
     * @param outputJarPath       Path where the modified JAR will be saved.
     * @param replacementsDirPath Path to the directory containing replacement class files.
     * @param resourcesDirPath    Path to the directory containing new resources (images, models, etc.).
     * @param classesToReplace    List of fully qualified class names to replace.
     * @throws IOException If an I/O error occurs.
     */
    public static void replaceStuffInJar(String inputJarPath, String outputJarPath,
                                         String replacementsDirPath, String resourcesDirPath,
                                         List<String> classesToReplace) throws IOException
    {
        // Convert class names to JAR entry paths
        Set<String> classEntryPaths = new HashSet<>();
        for (String className : classesToReplace)
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

        // Prepare to read the resources directory
        Path resourcesDir = Paths.get(resourcesDirPath);
        if (!Files.isDirectory(resourcesDir))
        {
            throw new IOException("Resources directory does not exist or is not a directory: " + resourcesDirPath);
        }

        // Track the entries already added to the new JAR
        Set<String> addedEntries = new HashSet<>();

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
                    // Replace this class
                    String classFilePath = replacementsDirPath + File.separator + entryName;
                    Path replacementClassPath = Paths.get(classFilePath);
                    if (!Files.exists(replacementClassPath))
                    {
                        throw new FileNotFoundException("Replacement class file not found: " + classFilePath);
                    }

                    byte[] replacementClassBytes = Files.readAllBytes(replacementClassPath);

                    // Use ASM to process the replacement class (optional: validation or modification)
                    byte[] processedClassBytes = processClassWithASM(replacementClassBytes);

                    // Write the replacement class bytes to the output JAR
                    outputJar.write(processedClassBytes);
                    System.out.println("Replaced class: " + entryName);
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

            // Handle adding new resources that do not exist in the original JAR
            addNewResources(outputJar, resourcesDir, addedEntries, inputJar);

            // Optionally, handle classes to replace that were not present in the original JAR by adding them as new entries
            for (String entryPath : classEntryPaths)
            {
                if (inputJar.getEntry(entryPath) == null)
                {
                    // Add the replacement class to the output JAR
                    String classFilePath = replacementsDirPath + File.separator + entryPath;
                    Path replacementClassPath = Paths.get(classFilePath);
                    if (!Files.exists(replacementClassPath))
                    {
                        throw new FileNotFoundException("Replacement class file not found: " + classFilePath);
                    }

                    byte[] replacementClassBytes = Files.readAllBytes(replacementClassPath);
                    byte[] processedClassBytes = processClassWithASM(replacementClassBytes);

                    JarEntry newClassEntry = new JarEntry(entryPath);
                    outputJar.putNextEntry(newClassEntry);
                    outputJar.write(processedClassBytes);
                    outputJar.closeEntry();

                    System.out.println("Added new class to JAR: " + entryPath);
                }
            }
        }
    }

    /**
     * Adds new resources (e.g., images, models) that are not already present in the JAR.
     *
     * @param outputJar    The JarOutputStream to write to.
     * @param resourcesDir The directory containing new resources to add.
     * @param addedEntries Set of entries already added to the new JAR (to avoid duplication).
     * @throws IOException If an I/O error occurs.
     */
    private static void addNewResources(JarOutputStream outputJar, Path resourcesDir, Set<String> addedEntries, JarFile inputJar) throws IOException
    {
        Files.walk(resourcesDir).forEach(path ->
        {
            if (!Files.isDirectory(path))
            {
                String entryName = resourcesDir.relativize(path).toString().replace("\\", "/"); // Ensure entry names use "/"
                if (!addedEntries.contains(entryName) && inputJar.getEntry(entryName) == null)
                {
                    try (InputStream is = Files.newInputStream(path))
                    {
                        JarEntry newEntry = new JarEntry(entryName);
                        outputJar.putNextEntry(newEntry);
                        copyStream(is, outputJar);
                        outputJar.closeEntry();
                        System.out.println("Added new resource to JAR: " + entryName);
                    }
                    catch (IOException e)
                    {
                        System.err.println("Failed to add resource: " + entryName);
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Processes a class file using ASM. This method reads the class bytes and writes them back,
     * effectively validating the class structure. You can extend this method to perform additional transformations if needed.
     *
     * @param classBytes Original class bytes.
     * @return Processed class bytes.
     * @throws IOException If ASM fails to process the class.
     */
    private static byte[] processClassWithASM(byte[] classBytes) throws IOException
    {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        // No transformations are performed here. The class is simply read and written back.
        // You can insert custom ClassVisitor implementations here if modifications are needed.
        classReader.accept(classWriter, 0);

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
}
