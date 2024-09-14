package com.orangishcat.modapi;

import net.fabricmc.api.ModInitializer;

import java.util.logging.Logger;

public class ModAPI implements ModInitializer {
    public static final String MOD_ID = "modapi";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    public static final String version = "Mod API v1.2.8";

    @Override
    public void onInitialize() {
        LOGGER.info("Hello World!");
    }
}
