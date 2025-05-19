package com.mocretion.blockpalettes.plugins.emi;

import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class EmiClient implements EmiPlugin {
    @Override
    public void register(EmiRegistry emiRegistry) {
        emiRegistry.addDragDropHandler(PaletteEditScreen.class, new EmiGhostHandler());
    }
}