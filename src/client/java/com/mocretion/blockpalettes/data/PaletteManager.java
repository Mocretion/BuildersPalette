package com.mocretion.blockpalettes.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mocretion.blockpalettes.BlockPalettesClient;
import com.mocretion.blockpalettes.data.helper.JsonHelper;
import com.mocretion.blockpalettes.data.helper.SaveHelper;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaletteManager {
    private static List<Palette> builderPalettes = new ArrayList<>();
    private static Map<Integer, Palette> selectedPalettes = new HashMap<>();
    private static boolean isEnabled = true;
    private static boolean isLargeMenu = true;
    private static boolean isInitialized = false;

    public PaletteManager(JsonObject json){

        isEnabled = json.get("isEnabled").getAsBoolean();

        if(!json.has("isLargeMenu"))
            isLargeMenu = true;
        else
            isLargeMenu = json.get("isLargeMenu").getAsBoolean();

        JsonArray palettesJson = json.getAsJsonArray("palettes");
        for(JsonElement jsonPalette : palettesJson){
            getBuilderPalettes().add(new Palette(jsonPalette.getAsJsonObject()));
        }

        JsonArray selectedJson = json.getAsJsonArray("selectedPalettes");
        for(JsonElement selectedElement : selectedJson){
            addSelectedPalettes(getBuilderPalettes().get(selectedElement.getAsInt()));
        }
    }

    public static List<Palette> getBuilderPalettes() {
        return builderPalettes;
    }

    public static List<Palette> getBuilderPalettes(String filter) {

        if(filter.startsWith("#"))
            return builderPalettes.stream().filter(p -> p.getIconName().toLowerCase().startsWith(filter.substring(1).toLowerCase())).toList();

        return builderPalettes.stream().filter(p -> p.getName().toLowerCase().startsWith(filter.toLowerCase())).toList();
    }

    public static Map<Integer, Palette> getSelectedPalettes(){
        return selectedPalettes;
    }

    public static void deletePalette(int id){
        Palette palette = getBuilderPalettes().get(id);
        if(selectedPalettes.containsKey(palette.getHotbarSlot())){
            if(selectedPalettes.get(palette.getHotbarSlot()) == palette){
                selectedPalettes.remove(palette.getHotbarSlot());
            }
        }

        getBuilderPalettes().remove(id);
    }

    public static void changePaletteHotbarSlot(Palette palette, int newSlot){

        // Remove old occurrence
        if(selectedPalettes.containsKey(palette.getHotbarSlot())){
            if(selectedPalettes.get(palette.getHotbarSlot()) == palette){
                selectedPalettes.remove(palette.getHotbarSlot());
                selectedPalettes.put(newSlot, palette);
            }
        }
    }

    /**
     * Adds a palette to selection. If a palette is already added, it gets removed from selection
     * @param palette Palette to add
     * @return True if added, False if removed
     */
    public static boolean addSelectedPalettes(Palette palette){

        // Deselect
        if(selectedPalettes.containsKey(palette.getHotbarSlot())){
            if(selectedPalettes.get(palette.getHotbarSlot()) == palette){
                selectedPalettes.remove(palette.getHotbarSlot());
                return false;
            }
        }

        selectedPalettes.put(palette.getHotbarSlot(), palette);
        SaveHelper.saveSettings();
        return true;
    }

    public static boolean isPaletteSelected(Palette palette){
        if(selectedPalettes.containsKey(palette.getHotbarSlot())){
            return selectedPalettes.get(palette.getHotbarSlot()) == palette;
        }
        return false;
    }

    public static void deselectAllPalettes(){
        selectedPalettes.clear();
    }

    public static void importPalette(){

        Minecraft client = Minecraft.getInstance();

        if (client.getWindow() != null) {
            long window = client.getWindow().getWindow();
            String clipboardContent = GLFW.glfwGetClipboardString(window);
            if(clipboardContent == null)
                return;

            JsonObject newJsonPalette = JsonHelper.stringToJson(clipboardContent, true);
            if(newJsonPalette == null)  // Signature missing
                return;

            getBuilderPalettes().add(new Palette(newJsonPalette));
        }
    }

    public static boolean getIsEnabled(){
        return isEnabled;
    }

    public static boolean toggleEnabled(){
        isEnabled = !isEnabled;
        return isEnabled;
    }

    public static void setIsEnabled(boolean enable){
        isEnabled = enable;
    }

    public static void toggleLayout(){
        isLargeMenu = !isLargeMenu;
    }

    public static boolean isLargeMenu(){
        return isLargeMenu;
    }

    public static JsonObject toJson(){
        JsonObject json = new JsonObject();
        JsonArray jsonPalettes = new JsonArray(getBuilderPalettes().size());
        JsonArray jsonSelectedPalettes = new JsonArray(getSelectedPalettes().size());

        for(Palette palette : getBuilderPalettes()){
            jsonPalettes.add(palette.toJson());
        }
        json.add("palettes", jsonPalettes);

        for(Palette selectedPalette : selectedPalettes.values()){
            for (int i = 0; i < getBuilderPalettes().size(); i++) {
                if(getBuilderPalettes().get(i) == selectedPalette){
                    jsonSelectedPalettes.add(i);
                    break;
                }
            }
        }
        json.add("selectedPalettes", jsonSelectedPalettes);

        json.addProperty("isEnabled", isEnabled);
        json.addProperty("isLargeMenu", isLargeMenu);

        return json;
    }

    public static void initBySaveFile(){
        if(!isInitialized){
            SaveHelper.loadSettings();
            isInitialized = true;
        }
    }
}
