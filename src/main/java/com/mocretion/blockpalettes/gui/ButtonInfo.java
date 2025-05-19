package com.mocretion.blockpalettes.gui;

import net.minecraft.resources.ResourceLocation;

public class ButtonInfo{
    public int u;
    public int v;
    public ResourceLocation identifier;

    public ButtonInfo(ResourceLocation identifier, int u, int v){
        this.u = u;
        this.v = v;
        this.identifier = identifier;
    }
}
