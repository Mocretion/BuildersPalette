package com.mocretion.builderspalette.gui.hud;

import com.mocretion.builderspalette.data.Palette;
import com.mocretion.builderspalette.data.PaletteManager;
import com.mocretion.builderspalette.gui.ButtonCatalogue;
import com.mocretion.builderspalette.gui.ButtonInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class HudRenderer {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static final int HOTBARR_WIDTH = 178;
    private static final int HOOTBAR_HEIGHT = 20;

    private static final int OVERLAY_HEIGHT = 18;
    private static final int OVERLAY_Width = 18;

    public void renderHudAdditions(DrawContext context, RenderTickCounter renderTickCounter) {

        if(!PaletteManager.getIsEnabled() || PaletteManager.getSelectedPalettes().isEmpty())
            return;

        int screenWidth  = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int x = (screenWidth - HOTBARR_WIDTH) / 2;
        int y = screenHeight - HOOTBAR_HEIGHT;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for(int enabledSlot : PaletteManager.getSelectedPalettes().keySet()){
            ButtonInfo hudElement = ButtonCatalogue.getHotbarActivePalette(enabledSlot -1);
            context.drawTexture(hudElement.identifier, x + 20 * (enabledSlot - 1), y, hudElement.u, hudElement.v, OVERLAY_Width, OVERLAY_HEIGHT);
        }

        RenderSystem.disableBlend();
    }
}
