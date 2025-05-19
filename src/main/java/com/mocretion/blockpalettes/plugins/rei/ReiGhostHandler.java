package com.mocretion.blockpalettes.plugins.rei;

import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public class ReiGhostHandler implements DraggableStackVisitor<PaletteEditScreen> {

    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<PaletteEditScreen> context, DraggableStack stack) {

        if(stack.getStack().isEmpty() || !(stack.getStack().getValue() instanceof ItemStack))
            return DraggableStackVisitor.super.acceptDraggedStack(context, stack);

        ItemStack is = (ItemStack) stack.getStack().getValue();

        context.getScreen().setDraggedStack(is);

        context.getScreen().mouseReleased(context.getCurrentPosition().getX(), context.getCurrentPosition().getY(), 0);

        return DraggedAcceptorResult.ACCEPTED;
    }

    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return true;
    }
}