package com.mocretion.blockpalettes.gui.screens.menutypes;

import com.mocretion.blockpalettes.BlockPalettesClient;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class AllMenus {

    public static final MenuType<EditMenu> EDIT_MENU_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "palette_edit"),
                new MenuType<>(EditMenu::new, FeatureFlags.DEFAULT_FLAGS)

     );

}
