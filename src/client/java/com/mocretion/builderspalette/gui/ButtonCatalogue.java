package com.mocretion.builderspalette.gui;

import com.mocretion.builderspalette.BuildersPaletteClient;
import net.minecraft.util.Identifier;

public class ButtonCatalogue {

    private static final Identifier BUTTON_TEXTURE = Identifier.of(BuildersPaletteClient.MOD_ID, "textures/gui/buttons.png");
    private static final Identifier HUD_TEXTURE = Identifier.of(BuildersPaletteClient.MOD_ID, "textures/gui/hotbar_hud.png");

    public static final int smallButtonSize = 14;

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

    // ====== HUD ======
    public static ButtonInfo getHotbarActivePalette(int i){
        return new ButtonInfo(HUD_TEXTURE, 18 * i, 0);
    }
}

