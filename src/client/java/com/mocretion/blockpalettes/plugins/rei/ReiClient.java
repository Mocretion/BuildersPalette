package com.mocretion.blockpalettes.plugins.rei;

import com.mocretion.blockpalettes.BlockPalettesClient;
import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;

public class ReiClient implements REIClientPlugin {

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new ReiGhostHandler());
    }
}