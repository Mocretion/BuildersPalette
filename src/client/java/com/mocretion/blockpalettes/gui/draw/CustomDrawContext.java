package com.mocretion.blockpalettes.gui.draw;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

public class CustomDrawContext {

    private final DrawContext context;
    private final MinecraftClient client;

    public CustomDrawContext(MinecraftClient client, DrawContext context) {
        this.client = client;
        this.context = context;
    }

    public void drawItem(ItemStack stack, int x, int y, float scale){
        if (!stack.isEmpty()) {
            BakedModel bakedModel = this.client.getItemRenderer().getModel(stack, this.client.world, this.client.player, 0);
            context.getMatrices().push();
            context.getMatrices().translate((float)(x + 8), (float)(y + 8), (float)(150));

            try {
                context.getMatrices().scale(scale, -scale, scale);
                boolean bl = !bakedModel.isSideLit();
                if (bl) {
                    DiffuseLighting.disableGuiDepthLighting();
                }

                this.client
                        .getItemRenderer()
                        .renderItem(stack, ModelTransformationMode.GUI, false, context.getMatrices(), context.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
                context.draw();
                if (bl) {
                    DiffuseLighting.enableGuiDepthLighting();
                }
            } catch (Throwable var12) {
                CrashReport crashReport = CrashReport.create(var12, "Rendering item");
                CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
                crashReportSection.add("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportSection.add("Item Components", () -> String.valueOf(stack.getComponents()));
                crashReportSection.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
                throw new CrashException(crashReport);
            }

            context.getMatrices().pop();
        }
    }
}
