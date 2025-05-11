package com.mocretion.builderspalette.gui;

import net.minecraft.util.Identifier;

public class ButtonInfo{
    public int u;
    public int v;
    public Identifier identifier;

    public ButtonInfo(Identifier identifier, int u, int v){
        this.u = u;
        this.v = v;
        this.identifier = identifier;
    }
}
