package com.mocretion.blockpalettes.gui.screens.menutypes;

import com.mocretion.blockpalettes.BlockPalettes;
import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class AllMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, BlockPalettes.MOD_ID);

    public static final Supplier<MenuType<EditMenu>> EDIT_MENU =
            MENU_TYPES.register("palette_edit", () -> new MenuType<>(EditMenu::new, FeatureFlags.DEFAULT_FLAGS));


    public static void register(IEventBus modEventBus){
        MENU_TYPES.register(modEventBus);
    }

    @EventBusSubscriber(modid = BlockPalettes.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientRegistry {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Register screen factory
            event.enqueueWork(() -> {
                MenuScreens.create(EDIT_MENU.get(), Minecraft.getInstance(), 346346375, Component.literal(""));
            });
        }
    }
}
