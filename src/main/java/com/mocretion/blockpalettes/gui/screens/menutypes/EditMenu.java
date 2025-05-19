package com.mocretion.blockpalettes.gui.screens.menutypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.IContainerFactory;
import org.jetbrains.annotations.Nullable;

public class EditMenu extends AbstractContainerMenu {


    public static final MenuType<EditMenu> TYPE = new MenuType<>(EditMenu::new, FeatureFlags.DEFAULT_FLAGS);

    public EditMenu(int syncId, Inventory playerInventory) {
        super(TYPE, syncId);

        this.addSlot(new Slot(new SimpleContainer(1), 0, -50, -50));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
