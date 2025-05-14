package com.mocretion.blockpalettes.plugins.emi;

import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class EmiGhostHandler implements EmiDragDropHandler<PaletteEditScreen> {
    @Override
    public boolean dropStack(PaletteEditScreen paletteEditScreen, EmiIngredient emiIngredient, int x, int y) {

        if(emiIngredient.isEmpty() || emiIngredient.getEmiStacks().isEmpty())
            return false;

        EmiStack emiStack = emiIngredient.getEmiStacks().getFirst();

        if(emiStack.isEmpty())
            return false;

        if(emiStack.getItemStack().isEmpty())
            return false;

        paletteEditScreen.setDraggedStack(emiStack.getItemStack());
        paletteEditScreen.mouseReleased(x, y, 0);
        return true;
    }

    @Override
    public void render(PaletteEditScreen screen, EmiIngredient dragged, GuiGraphics draw, int mouseX, int mouseY, float delta) {
        EmiDragDropHandler.super.render(screen, dragged, draw, mouseX, mouseY, delta);
    }
}
