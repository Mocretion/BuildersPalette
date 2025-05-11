package com.mocretion.builderspalette.data.helper;

import com.google.gson.*;
import com.mocretion.builderspalette.BuildersPaletteClient;
import com.mocretion.builderspalette.data.Palette;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.MinecraftClient;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;

import java.util.Optional;

public class JsonHelper {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String jsonToString(JsonElement json){
        return jsonToString(json, false);
    }

    public static String jsonToString(JsonElement json, boolean withSignature){
        return withSignature ? BuildersPaletteClient.MOD_NAME + GSON.toJson(json) : GSON.toJson(json);
    }

    public static JsonObject stringToJson(String jsonString, boolean onlyWithSignature){

        if(onlyWithSignature){
            if(jsonString.startsWith(BuildersPaletteClient.MOD_NAME)){
                jsonString = jsonString.substring(BuildersPaletteClient.MOD_NAME.length());
            }else{
                return null;
            }
        }

        return JsonParser.parseString(jsonString).getAsJsonObject();
    }

    public static String itemStackToJson(ItemStack stack){
        JsonElement json = itemStackToJsonObject(stack);
        return GSON.toJson(json);
    }

    public static JsonElement itemStackToJsonObject(ItemStack stack) {

        DynamicRegistryManager registryManager = getRegistryManager();

        RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, registryManager);

        Optional<JsonElement> result = ItemStack.CODEC
                .encodeStart(registryOps, stack)
                .resultOrPartial(error -> {
                    System.err.println("Error encoding ItemStack to JSON: " + error);
                });

        return result.orElseGet(() -> {
            // Fallback of encoding fails
            JsonObject fallback = new JsonObject();
            fallback.addProperty("error", "Failed to encode ItemStack");
            return fallback;
        });
    }

    public static ItemStack jsonToItemStack(JsonElement jsonItemStack) {

        DynamicRegistryManager registryManager = getRegistryManager();

        RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, registryManager);

        Optional<Pair<ItemStack, JsonElement>> result = ItemStack.CODEC
                .decode(registryOps, jsonItemStack)
                .resultOrPartial(error -> {
                    System.err.println("Error encoding ItemStack to JSON: " + error);
                });

        ItemStack resultStack = result.orElseGet(() -> {
            // Fallback of decoding fails
            return new Pair<>(ItemStack.EMPTY, jsonItemStack);
        }).getFirst();

        return resultStack;
    }

    /**
     * Clientside only!!
     */
    private static DynamicRegistryManager getRegistryManager() {
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().world != null) {
            return MinecraftClient.getInstance().world.getRegistryManager();
        }

        throw new IllegalStateException("Unable to access registry manager. Make sure this code is running in a world context.");
    }
}
