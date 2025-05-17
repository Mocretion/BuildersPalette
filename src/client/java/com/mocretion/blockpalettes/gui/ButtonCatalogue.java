package com.mocretion.blockpalettes.gui;

import com.mocretion.blockpalettes.BlockPalettesClient;
import net.minecraft.resources.ResourceLocation;

public class ButtonCatalogue {

    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/buttons.png");
    private static final ResourceLocation HUD_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/hotbar_hud.png");

    public static final int smallButtonSize = 14;
    public static final int xsButtonSize = 10;

    public static ButtonInfo getDeleteConfirm(){
        return new ButtonInfo(BUTTON_TEXTURE, 0, 0);
    }

    public static ButtonInfo getDeleteHover(){
        return new ButtonInfo(BUTTON_TEXTURE, smallButtonSize, 0);
    }

    public static ButtonInfo getEditHover(){
        return new ButtonInfo(BUTTON_TEXTURE, smallButtonSize * 2, 0);
    }

    public static ButtonInfo getTogglePalettesHover(){
        return new ButtonInfo(BUTTON_TEXTURE, smallButtonSize * 3, 0);
    }

    public static ButtonInfo getDeselectAllHover(){
        return new ButtonInfo(BUTTON_TEXTURE, smallButtonSize * 4, 0);
    }

    public static ButtonInfo getImportHover(){
        return new ButtonInfo(BUTTON_TEXTURE, smallButtonSize * 5, 0);
    }

    public static ButtonInfo getExportHover(){
        return new ButtonInfo(BUTTON_TEXTURE, smallButtonSize * 6, 0);
    }


    // ====== Next Row ======
    public static ButtonInfo getSelectionButton(int i){
        return new ButtonInfo(BUTTON_TEXTURE, smallButtonSize * i, smallButtonSize);
    }

    // ====== Next Row ======
    public static ButtonInfo getSelectionButtonXs(int i){
        return new ButtonInfo(BUTTON_TEXTURE, xsButtonSize * i, smallButtonSize * 2);
    }

    public static ButtonInfo getToggleLayoutHover(){
        return new ButtonInfo(BUTTON_TEXTURE, xsButtonSize * 9, smallButtonSize * 2);
    }

    // ====== HUD ======
    public static ButtonInfo getHotbarActivePalette(int i){
        return new ButtonInfo(HUD_TEXTURE, 18 * i, 0);
    }
}

