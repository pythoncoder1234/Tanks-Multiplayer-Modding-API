package tanks;

import tanks.gui.TextBox;
import tanks.gui.screen.ScreenTankEditor;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TankReferenceSolver
{
    public static boolean useTankReferences = true;
    public static HashMap<String, Field> fieldMap;
    public static ArrayList<Field> referenceFields = new ArrayList<>();
    public static HashMap<String, TankValidationFunc> validateMap = new HashMap<>();

    static
    {
        validateMap.put("name", (inputText, tank, editor) ->
        {
            if (inputText.equals("player"))
                return new Message(false, "Psst... \"player\" is the name of the default player spawn marker, pick a different name...");

            if (Game.registryTank.tankEntries.stream().anyMatch(t -> t.name.equals(inputText)))
                return new Message(false, "\"" + inputText + "\" matches the name of the default tanks, pick a different name...");

            if (editor.level.customTanks.stream().anyMatch(t -> inputText.equals(t.name)))
                return new Message(false, "A tank with this name already exists, pick a different name...");

            ArrayList<TankAIControlled> refs = getReferences(tank);

            for (TankAIControlled t : refs)
            {
                for (Field f : referenceFields)
                {
                    try
                    {
                        Tank t1 = (Tank) f.get(t);
                        if (t1 != null && t1.name.equals(tank.name))
                            f.set(t, tank);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }

                for (TankAIControlled.SpawnedTankEntry entry : t.spawnedTankEntries)
                {
                    if (entry.tank.name.equals(tank.name))
                        entry.tank = tank;
                }
            }
            return Message.valid;
        });
    }

    private static final ArrayList<TankAIControlled> newList = new ArrayList<>();

    public static ArrayList<TankAIControlled> getReferences(TankAIControlled t)
    {
        if (t.name == null)
            return newList;
        return Game.currentLevel.references.getOrDefault(t, newList);
    }

    public static void solveAllReferences(Level l, Map<String, TankAIControlled> tankMap)
    {
        for (TankAIControlled t : l.valueTanks)
        {
            t.solve();

            if (t.solved() && !tankMap.containsKey(t.name))
            {
                l.customTanks.add(t);
                tankMap.put(t.name, t);
            }
        }
    }

    private static final TankValidationFunc alwaysNull = (inputText, tank, editor) -> new Message(true, null);

    @FunctionalInterface
    public interface TankValidationFunc
    {
        Message test(String inputText, TankAIControlled t, ScreenLevelEditor s);
    }

    public static Message testInput(Field f, TankProperty p, TankAIControlled tank, TextBox t, ScreenLevelEditor editor)
    {
        return TankReferenceSolver.validateMap.getOrDefault(p.id(), alwaysNull).test(t.inputText, tank, editor);
    }

    public record Message(boolean isValid, String message)
    {
        public static Message valid = new Message(true, null);

        public String[] formatMessage()
        {
            return ScreenTankEditor.formatDescription(message);
        }
    }
}
