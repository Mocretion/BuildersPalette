package com.mocretion.blockpalettes.data.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mocretion.blockpalettes.BlockPalettesClient;
import com.mocretion.blockpalettes.data.PaletteManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SaveHelper {

    private static final String CONFIG_FILENAME = BlockPalettesClient.MOD_NAME + ".json";

    private static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(BlockPalettesClient.MOD_ID);
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
            String stringSettings = Files.readString(configFile.toPath(), StandardCharsets.UTF_16);
            JsonObject jsonSettings = JsonParser.parseString(stringSettings).getAsJsonObject();
            new PaletteManager(jsonSettings);
        }catch (Exception e){
            BlockPalettesClient.LOGGER.warn(e.getMessage());
        }
    }

    public static void saveSettings() {
        try {
            Files.writeString(getConfigFile().toPath(), JsonHelper.jsonToString(PaletteManager.toJson()), StandardCharsets.UTF_16);
        }catch(Exception e){}
    }
}
