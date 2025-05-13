package com.mocretion.blockpalettes.plugins.jei;

import com.mocretion.blockpalettes.BlockPalettesClient;
import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GhostHandler implements IGhostIngredientHandler<PaletteEditScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(PaletteEditScreen paletteEditScreen, ITypedIngredient<I> iTypedIngredient, boolean b) {

        BlockPalettesClient.LOGGER.info(iTypedIngredient.getIngredient().getClass().toString());

        if(iTypedIngredient == null || iTypedIngredient.getItemStack().isEmpty())
            return new ArrayList<>();

        ItemStack stack = iTypedIngredient.getItemStack().get();

        paletteEditScreen.setDraggedStack(stack);

        return new ArrayList<>();
    }

    @Override
    public void onComplete() {

    }
}
