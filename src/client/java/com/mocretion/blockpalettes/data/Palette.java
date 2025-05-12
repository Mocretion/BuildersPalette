package com.mocretion.blockpalettes.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mocretion.blockpalettes.BlockPalettesClient;
import com.mocretion.blockpalettes.data.helper.JsonHelper;
import com.mocretion.blockpalettes.data.helper.SaveHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Palette {
    private String name;
    private ItemStack icon;
    private int hotbarSlot;
    private List<WeightCategory> weights;

    public Palette(String name, ItemStack icon, int hotbarSlot, List<WeightCategory> weights) {
        this.name = name;
        this.icon = icon;
        this.hotbarSlot = hotbarSlot;
        this.weights = weights;
    }

    public Palette(JsonObject jsonPalette){
        this.name = jsonPalette.get("name").getAsString();
        ItemStack iconStack = JsonHelper.jsonToItemStack(jsonPalette.get("icon"));
        this.icon = iconStack.isEmpty() ? new ItemStack(Items.GRASS_BLOCK) : iconStack;  // Failed to decode => empty
        this.hotbarSlot = jsonPalette.get("hotbarSlot").getAsInt();
        this.weights = new ArrayList<>();

        for(JsonElement jsonWeight : jsonPalette.getAsJsonArray("weights")){
            this.weights.add(new WeightCategory(jsonWeight.getAsJsonObject()));
        }
    }

    public String getName() {
        return name;
    }

    public String getShortenedName(TextRenderer renderer, int maxWidth){
        if(renderer.getWidth(getName()) > maxWidth){
            String trimmed = renderer.trimToWidth(getName(), maxWidth);
            trimmed = trimmed.substring(0, trimmed.length() - 4) + "...";
            return trimmed;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public int getHotbarSlot() {
        return hotbarSlot;
    }

    public void setHotbarSlot(int hotbarSlot) {
        PaletteManager.changePaletteHotbarSlot(this, hotbarSlot);
        this.hotbarSlot = hotbarSlot;
        SaveHelper.saveSettings();
    }

    public List<WeightCategory> getWeights() {
        return weights;
    }

    public void setWeights(List<WeightCategory> weights) {
        this.weights = weights;
    }

    public void addWeight(WeightCategory weight){
        this.weights.add(weight);
    }

    public void removeWeight(int i) {
        getWeights().remove(i);
    }

    public int getScreenRowCount(){
        int count = this.weights.size() * 2;  // Header + first row

        for(WeightCategory cat : this.weights){
            count += cat.getItems().size() / 9;
            if(cat.getItems().size() % 9 == 0 && !cat.getItems().isEmpty())
                count++;  // Empty row
        }

        return count;
    }

    public void applyWeightInputField(){
        for(WeightCategory cat : this.weights){
            cat.setWeightInputField(Integer.toString(cat.getWeight()));
        }
    }

    public void getPaletteItemFromInventory(PlayerEntity player){

        int hotbarSlot = this.hotbarSlot;  // Arrays start at 0, hotbarSlot at 1.
        hotbarSlot--;

        int totalWeight = 0;
        for(WeightCategory cat : getWeights()){
            totalWeight += cat.getWeight();
        }

        if(totalWeight == 0)
            return;

        int randomWeight = BlockPalettesClient.random.nextBetween(1, totalWeight);
        totalWeight = 0;
        WeightCategory selectedWeightCat = null;
        for(WeightCategory cat : getWeights()){
            totalWeight += cat.getWeight();

            if(totalWeight >= randomWeight){
                selectedWeightCat = cat;
                break;
            }
        }

        if(selectedWeightCat == null)
            return;

        if(selectedWeightCat.getItems().isEmpty())
            return;

        int randomBlock = BlockPalettesClient.random.nextBetween(0, selectedWeightCat.getItems().size() - 1);
        ItemStack randomStack = selectedWeightCat.getItems().get(randomBlock);

        PlayerInventory playerInv = player.getInventory();
        ScreenHandler screenHandler = player.playerScreenHandler;

        if(player.getAbilities().creativeMode){
            CreativeInventoryActionC2SPacket packet =
                    new CreativeInventoryActionC2SPacket(hotbarSlot + 36, randomStack);
            ((ClientPlayerEntity)player).networkHandler.sendPacket(packet);
        }else {

            for (int i = 0; i < playerInv.main.size(); i++) {

                int slot = i < 9 ? i + 36 : i;
                ItemStack playerStack = playerInv.main.get(i);

                if (ItemStack.areItemsAndComponentsEqual(playerStack, randomStack)) {
                    MinecraftClient.getInstance().interactionManager.clickSlot(screenHandler.syncId, slot, hotbarSlot, SlotActionType.SWAP, player);
                    break;
                }
            }
        }
    }

    public void exportToClipboard(){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getWindow() != null) {
            long window = client.getWindow().getHandle();
            GLFW.glfwSetClipboardString(window, JsonHelper.jsonToString(toJson(), true));
        }
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();
        json.addProperty("name", getName());
        json.add("icon", JsonHelper.itemStackToJsonObject(getIcon()));
        json.addProperty("hotbarSlot", getHotbarSlot());

        JsonArray jsonWeights = new JsonArray(getWeights().size());
        for(WeightCategory cat : getWeights()){
            jsonWeights.add(cat.toJson());
        }

        json.add("weights", jsonWeights);

        return json;
    }
}
