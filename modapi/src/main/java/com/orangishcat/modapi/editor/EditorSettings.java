package com.orangishcat.modapi.editor;

import com.orangishcat.modapi.editor.selector.LevelEditorSelector;
import com.orangishcat.modapi.gui.screen.ScreenLevelEditor;

import java.util.HashMap;

public class EditorSettings
{
    public static HashMap<String, LevelEditorSelector<?>> selectors = new HashMap<>();
    public static ScreenLevelEditor.Placeable currentPlaceable = ScreenLevelEditor.Placeable.enemyTank;
    public static EditorClipboard[] clipboards = new EditorClipboard[5];
    public static EditorClipboard clipboard = new EditorClipboard();
    public static int selectedNum = 0;
}
