package com.mocretion.blockpalettes.plugins.emi;

import com.mocretion.blockpalettes.BlockPalettesClient;
import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;
import com.mocretion.blockpalettes.gui.screens.menutypes.AllMenus;
import com.mocretion.blockpalettes.gui.screens.menutypes.EditMenu;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@EmiEntrypoint
public class EmiClient implements EmiPlugin {
    @Override
    public void register(EmiRegistry emiRegistry) {
        emiRegistry.addDragDropHandler(PaletteEditScreen.class, new EmiGhostHandler());
    }
}
