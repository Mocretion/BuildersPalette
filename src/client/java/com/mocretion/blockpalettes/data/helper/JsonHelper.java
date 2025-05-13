package com.mocretion.blockpalettes.data.helper;

import com.google.gson.*;
import com.mocretion.blockpalettes.BlockPalettesClient;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class JsonHelper {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String jsonToString(JsonElement json){
        return jsonToString(json, false);
    }

    public static String jsonToString(JsonElement json, boolean withSignature){
        return withSignature ? BlockPalettesClient.MOD_NAME + GSON.toJson(json) : GSON.toJson(json);
    }

    public static JsonObject stringToJson(String jsonString, boolean onlyWithSignature){

        if(onlyWithSignature){
            if(jsonString.startsWith(BlockPalettesClient.MOD_NAME)){
                jsonString = jsonString.substring(BlockPalettesClient.MOD_NAME.length());
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

        RegistryAccess registryManager = getRegistryManager();

        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryManager);

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

        RegistryAccess registryManager = getRegistryManager();

        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryManager);

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
    private static RegistryAccess getRegistryManager() {
        if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.registryAccess();
        }

        throw new IllegalStateException("Unable to access registry manager. Make sure this code is running in a world context.");
    }
}
