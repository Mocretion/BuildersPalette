package com.mocretion.builderspalette.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mocretion.builderspalette.data.helper.JsonHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WeightCategory {
    private int weight;
    private String weightInputField;
    private List<ItemStack> items;

    public WeightCategory(int weight, List<ItemStack> items){
        this.weight = weight;
        this.items = items;
        this.weightInputField = getWeightText();
    }

    public WeightCategory(JsonObject jsonWeight){
        this.weight = jsonWeight.get("weight").getAsInt();
        this.weightInputField = getWeightText();

        this.items = new ArrayList<>();

        for(JsonElement jsonItem : jsonWeight.getAsJsonArray("items")){
            ItemStack item = JsonHelper.jsonToItemStack(jsonItem);
            if(!item.isEmpty())  // Might be empty if failed to decode
                addItem(item);
        }
    }

    public int getWeight() {
        return weight;
    }

    public String getWeightText() {
        return Integer.toString(weight);
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    public void addItem(ItemStack item) {
        items.add(item);
    }

    public void removeItem(ItemStack item){
        items.remove(item);
    }

    public String getWeightInputField(){
        return weightInputField;
    }

    public void setWeightInputField(String text){
        weightInputField = text;

        if(weightInputField.isBlank()){
            setWeight(0);
            return;
        }

        try{
            setWeight(Integer.parseInt(weightInputField));
        }catch(Exception e){
            setWeight(0);
        }
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();
        JsonArray jsonItems = new JsonArray(getItems().size());

        for(ItemStack itemStack : getItems()){
            jsonItems.add(JsonHelper.itemStackToJsonObject(itemStack));
        }

        json.add("items", jsonItems);
        json.addProperty("weight", getWeight());

        return json;
    }
}
