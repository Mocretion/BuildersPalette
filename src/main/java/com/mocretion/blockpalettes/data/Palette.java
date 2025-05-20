package com.mocretion.blockpalettes.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mocretion.blockpalettes.BlockPalettes;
import com.mocretion.blockpalettes.data.helper.JsonHelper;
import com.mocretion.blockpalettes.data.helper.SaveHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    public String getShortenedName(Font renderer, int maxWidth){
        if(renderer.width(getName()) > maxWidth){
            String trimmed = renderer.plainSubstrByWidth(getName(), maxWidth);
            trimmed = trimmed.substring(0, trimmed.length() - 3) + "...";
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

    public String getIconName(){
        String itemName = getIcon().getDisplayName().getString();  // returns [item_name_or_renamed_name] (with brackets)
        itemName = itemName.substring(1, itemName.length() - 1);  // remove brackets
        return itemName;
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

    public void getPaletteItemFromInventory(Player player){

        int hotbarSlot = this.hotbarSlot;  // Arrays start at 0, hotbarSlot at 1.
        hotbarSlot--;

        int totalWeight = 0;
        for(WeightCategory cat : getWeights()){
            totalWeight += cat.getWeight();
        }

        if(totalWeight == 0)
            return;

        // 5 tries to select block
        for (int t = 0; t < 5; t++) {

            int randomWeight = BlockPalettes.random.nextIntBetweenInclusive(1, totalWeight);
            int weight = 0;
            WeightCategory selectedWeightCat = null;
            for (WeightCategory cat : getWeights()) {
                weight += cat.getWeight();

                if (weight >= randomWeight) {
                    selectedWeightCat = cat;
                    break;
                }
            }

            if (selectedWeightCat == null)
                return;

            if (selectedWeightCat.getItems().isEmpty())
                return;

            int randomBlock = BlockPalettes.random.nextIntBetweenInclusive(0, selectedWeightCat.getItems().size() - 1);
            ItemStack randomStack = selectedWeightCat.getItems().get(randomBlock);

            Inventory playerInv = player.getInventory();
            AbstractContainerMenu screenHandler = player.containerMenu;

            if (player.getAbilities().instabuild) {
                ServerboundSetCreativeModeSlotPacket packet =
                        new ServerboundSetCreativeModeSlotPacket(hotbarSlot + 36, randomStack);
                ((LocalPlayer) player).connection.send(packet);

                playerInv.setItem(hotbarSlot, randomStack.copy());

                return;
            } else {

                for (int i = 0; i < playerInv.getNonEquipmentItems().size(); i++) {

                    int slot = i < 9 ? i + 36 : i;
                    ItemStack playerStack = playerInv.getNonEquipmentItems().get(i);

                    if (ItemStack.isSameItemSameComponents(playerStack, randomStack)) {
                        Minecraft.getInstance().gameMode.handleInventoryMouseClick(screenHandler.containerId, slot, hotbarSlot, ClickType.SWAP, player);
                        return;
                    }
                }
            }

            player.displayClientMessage(Component.literal("§cBlock Palettes: " + randomStack.getDisplayName().getString()).append(Component.translatable("container.blockpalettes.itemNotFound")), true);
        }
    }

    public void exportToClipboard(){
        Minecraft client = Minecraft.getInstance();
        if (client.getWindow() != null) {
            long window = client.getWindow().getWindow();
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
