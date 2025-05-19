package com.mocretion.blockpalettes.plugins.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public class ReiClient implements REIClientPlugin {

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new ReiGhostHandler());
    }
}