package com.mocretion.blockpalettes.plugins.jei;

import com.mocretion.blockpalettes.BlockPalettes;
import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.config.IJeiConfigManager;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JustEnoughItemsClient implements IModPlugin {


    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(BlockPalettes.MOD_ID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        IModPlugin.super.registerItemSubtypes(registration);
    }

    @Override
    public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
        IModPlugin.super.registerFluidSubtypes(registration, platformFluidHelper);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        IModPlugin.super.registerIngredients(registration);
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        IModPlugin.super.registerIngredientAliases(registration);
    }

    @Override
    public void registerModInfo(IModInfoRegistration modAliasRegistration) {
        IModPlugin.super.registerModInfo(modAliasRegistration);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IModPlugin.super.registerCategories(registration);
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IModPlugin.super.registerVanillaCategoryExtensions(registration);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IModPlugin.super.registerRecipes(registration);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        IModPlugin.super.registerRecipeTransferHandlers(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        IModPlugin.super.registerRecipeCatalysts(registration);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

        registration.addGhostIngredientHandler(PaletteEditScreen.class, new JeiGhostHandler());

        IModPlugin.super.registerGuiHandlers(registration);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        IModPlugin.super.registerAdvanced(registration);
    }

    @Override
    public void registerRuntime(IRuntimeRegistration registration) {
        IModPlugin.super.registerRuntime(registration);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        IModPlugin.super.onRuntimeAvailable(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        IModPlugin.super.onRuntimeUnavailable();
    }

    @Override
    public void onConfigManagerAvailable(IJeiConfigManager configManager) {
        IModPlugin.super.onConfigManagerAvailable(configManager);
    }
}