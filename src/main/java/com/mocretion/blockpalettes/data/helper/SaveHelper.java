package com.mocretion.blockpalettes.data.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mocretion.blockpalettes.BlockPalettes;
import com.mocretion.blockpalettes.data.PaletteManager;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SaveHelper {

    private static final String CONFIG_FILENAME = BlockPalettes.MOD_NAME + ".json";

    private static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get().resolve(BlockPalettes.MOD_ID);
    }

    private static File getConfigFile() {
        Path configDir = getConfigDir();
        File dir = configDir.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return configDir.resolve(CONFIG_FILENAME).toFile();
    }

    public static void loadSettings() {

        try {
            File configFile = getConfigFile();
            BlockPalettes.LOGGER.info(configFile.toPath().toString());
            String stringSettings = Files.readString(configFile.toPath(), StandardCharsets.UTF_16);
            JsonObject jsonSettings = JsonParser.parseString(stringSettings).getAsJsonObject();
            new PaletteManager(jsonSettings);
        }catch (Exception e){
            BlockPalettes.LOGGER.info("AAAAAAAAAAAAAAAAAAAAAA");
            BlockPalettes.LOGGER.warn(e.getMessage());
        }
    }

    public static void saveSettings() {
        try {
            Files.writeString(getConfigFile().toPath(), JsonHelper.jsonToString(PaletteManager.toJson()), StandardCharsets.UTF_16);
        }catch(Exception e){}
    }
}
