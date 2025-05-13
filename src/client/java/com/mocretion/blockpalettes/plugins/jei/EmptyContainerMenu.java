package com.mocretion.blockpalettes.plugins.jei;

import com.mocretion.blockpalettes.data.helper.JsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EmptyContainerMenu extends AbstractContainerMenu {
    public EmptyContainerMenu() {
        super(MenuType.GENERIC_9x1, 0);
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
