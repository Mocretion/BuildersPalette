package com.mocretion.blockpalettes.gui.draw;


import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CustomDrawContext {

    private final GuiGraphics context;
    private final Minecraft client;

    public CustomDrawContext(Minecraft client, GuiGraphics context) {
        this.client = client;
        this.context = context;
    }

    public void drawItem(ItemStack stack, int x, int y, float scale){
        if (!stack.isEmpty()) {
            BakedModel bakedModel = this.client.getItemRenderer().getModel(stack, this.client.level, this.client.player, 0);
            context.pose().pushPose();
            context.pose().translate((float)(x + 8), (float)(y + 8), (float)(150));

            try {
                context.pose().scale(scale, -scale, scale);
                boolean bl = !bakedModel.usesBlockLight();
                if (bl) {
                    Lighting.setupForFlatItems();
                }

                this.client
                        .getItemRenderer()
                        .render(stack, ItemDisplayContext.GUI, false, context.pose(), context.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
                context.flush();
                if (bl) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable var12) {
                CrashReport crashReport = CrashReport.forThrowable(var12, "Rendering item");
                CrashReportCategory crashReportSection = crashReport.addCategory("Item being rendered");
                crashReportSection.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportSection.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                crashReportSection.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashReport);
            }

            context.pose().popPose();
        }
    }
}