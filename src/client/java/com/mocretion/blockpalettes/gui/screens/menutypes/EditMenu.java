package com.mocretion.blockpalettes.gui.screens.menutypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EditMenu extends AbstractContainerMenu {


    public EditMenu(int syncId, Inventory playerInventory) {
        super(AllMenus.EDIT_MENU_HANDLER, syncId);

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
