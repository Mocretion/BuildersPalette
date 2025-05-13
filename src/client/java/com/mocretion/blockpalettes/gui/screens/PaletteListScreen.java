package com.mocretion.blockpalettes.gui.screens;

import com.mocretion.blockpalettes.BlockPalettesClient;
import com.mocretion.blockpalettes.data.Palette;
import com.mocretion.blockpalettes.data.PaletteManager;
import com.mocretion.blockpalettes.data.WeightCategory;
import com.mocretion.blockpalettes.data.helper.SaveHelper;
import com.mocretion.blockpalettes.gui.ButtonCatalogue;
import com.mocretion.blockpalettes.gui.ButtonInfo;
import com.mocretion.blockpalettes.gui.draw.CustomDrawContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class PaletteListScreen extends Screen {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/palette_list.png");
    private static final ResourceLocation PALETTE_PREVIEW_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/palette_preview.png");
    private static final ResourceLocation ADD_PALETTE_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/add_palette.png");
    private static final ResourceLocation SCROLLER_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/scroller.png");

    // GUI dimensions
    private final int backgroundWidth = 195;
    private final int backgroundHeight = 256;

    private final int paletteContainerStartHeight = 16;
    private final int paletteContainerStartWidth = 7;
    private final int scrollMarginX = 175;
    private final int scrollMarginY = 17;
    private final int scrollerWidth = 12;
    private final int scrollerHeight = 15;
    private final int scrollbarHeight = 218;
    private final int paletteItemHeight = 55;
    private final int paletteItemWidth = 162;
    private final int paletteItemTitleMarginX = 46;
    private final int paletteItemTitleMarginY = 7;
    private final int paletteItemIconMargin = 4;
    private final int paletteItemIconPreviewMarginX = 45;
    private final int paletteItemIconPreviewMarginY = 18;
    private final int paletteEditMarginX = 130;
    private final int paletteEditMarginY = 2;
    private final int paletteDeleteMarginX = 146;
    private final int paletteDeleteMarginY = 2;
    private final int paletteHotbarMarginX = 18;
    private final int paletteHotbarMarginY = 39;
    private final int paletteButtonMarginY = 238;
    private final int paletteToggleEnabledMarginX = 7;
    private final int paletteDeselectAllMarginX = 23;
    private final int paletteImportMarginX = 39;
    private final int itemSlotSize = 18;

    private final int maxPaletteTitleWidth = 80;

    private static final byte DELETE_DOUBLE_CLICK_DURATION = 20;
    private byte deleteConfirm;
    private int toBeDeletedId;

    private int leftPos;
    private int topPos;

    // Scrolling
    private double scrollPosition;
    private boolean clickedOnScroller;

    private Minecraft client;

    public PaletteListScreen(Minecraft client) {
        super(Component.translatable("container.blockpalettes.palette_list"));
        this.client = client;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.backgroundWidth) / 2;
        this.topPos = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta){

        super.renderBackground(context, mouseX, mouseY, delta);

        // Draw background
        context.blit(BG_TEXTURE, leftPos, topPos, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);

        CustomDrawContext customDrawContext = new CustomDrawContext(client, context);

        // Draw title
        context.drawString(this.font, this.title,
                leftPos + (backgroundWidth - font.width(this.title)) / 2,
                topPos + 6, 0x404040, false);

        // Draw scroller
        final int scrollLevel = (int)scrollPosition;
        context.blit(SCROLLER_TEXTURE, leftPos + scrollMarginX, getCurrentScrollerYPosition(), 0, 0, scrollerWidth, scrollerHeight);


        // Draw visual inventory slots
        List<Palette> palettes = PaletteManager.getBuilderPalettes();
        int xPos = leftPos + paletteContainerStartWidth;
        for (int paletteNo = scrollLevel; paletteNo < scrollLevel + 4; paletteNo++) {

            int slotNo = paletteNo - scrollLevel;
            int yPos = topPos + paletteContainerStartHeight + slotNo * paletteItemHeight;

            if(paletteNo == PaletteManager.getBuilderPalettes().size()){  // Add new element
                context.blit(ADD_PALETTE_TEXTURE, xPos, yPos, 0, 0, paletteItemWidth, paletteItemHeight);

                if(isPointInRegion(xPos, yPos, paletteItemWidth, paletteItemHeight, mouseX, mouseY)){
                    context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.addPalette").toFlatList(), mouseX, mouseY);
                }

            }else if(paletteNo < PaletteManager.getBuilderPalettes().size()){  // List existing element
                Palette palette = palettes.get(paletteNo);
                context.blit(PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, 0, paletteItemWidth, paletteItemHeight);

                customDrawContext.drawItem(palette.getIcon(), xPos + paletteItemIconMargin + 8, yPos + paletteItemIconMargin + 8, 32F);

                int previewSlot = 0;
                for (int weightNo = 0; weightNo < palette.getWeights().size(); weightNo++) {
                    if(previewSlot == 6) break;

                    WeightCategory weightCat = palette.getWeights().get(weightNo);
                    for (int weightItem = 0; weightItem < weightCat.getItems().size(); weightItem++) {
                        if(previewSlot == 6) break;
                        ItemStack item = weightCat.getItems().get(weightItem);

                        context.renderItem(item, xPos + paletteItemIconPreviewMarginX + previewSlot * itemSlotSize, yPos + paletteItemIconPreviewMarginY);
                        previewSlot++;
                    }
                }

                // Draw edit hover
                if(isPointInRegion(xPos + paletteEditMarginX, yPos + paletteEditMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {
                    ButtonInfo btnInfo = ButtonCatalogue.getEditHover();
                    context.blit(btnInfo.identifier, xPos + paletteEditMarginX, yPos + paletteEditMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize);
                    context.blit(PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, paletteItemHeight, paletteItemWidth, paletteItemHeight);
                    context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.editPalette").toFlatList(), mouseX, mouseY);
                }  // Draw delete confirmation
                else if(this.deleteConfirm > 0 && this.toBeDeletedId == paletteNo){
                    ButtonInfo btnInfo = ButtonCatalogue.getDeleteConfirm();
                    context.blit(btnInfo.identifier, xPos + paletteDeleteMarginX, yPos + paletteDeleteMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize);
                    context.blit(PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, paletteItemHeight, paletteItemWidth, paletteItemHeight);
                    context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.deletePalette").toFlatList(), mouseX, mouseY);
                }  // Draw delete hover
                else if(isPointInRegion(xPos + paletteDeleteMarginX, yPos + paletteDeleteMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {
                    ButtonInfo btnInfo = ButtonCatalogue.getDeleteHover();
                    context.blit(btnInfo.identifier, xPos + paletteDeleteMarginX, yPos + paletteDeleteMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize);
                    context.blit(PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, paletteItemHeight, paletteItemWidth, paletteItemHeight);
                    context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.deletePalette").toFlatList(), mouseX, mouseY);
                }  // Show selection toolbar slot
                else if(isPointInRegion(xPos + paletteHotbarMarginX, yPos + paletteHotbarMarginY, ButtonCatalogue.smallButtonSize * 9, ButtonCatalogue.smallButtonSize, mouseX, mouseY)){
                    context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.selectHotbarSlot").toFlatList(), mouseX, mouseY);
                }
                // Draw hover texture
                else if(isPointInRegion(xPos, yPos, paletteItemWidth, paletteItemHeight, mouseX, mouseY)){
                    context.blit(PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, paletteItemHeight, paletteItemWidth, paletteItemHeight);
                    context.renderComponentTooltip(this.font, Component.literal(palette.getName()).toFlatList(), mouseX, mouseY);
                }

                // Draw selected texture
                if(PaletteManager.isPaletteSelected(palette)){
                    context.blit(PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, paletteItemHeight * 2, paletteItemWidth, paletteItemHeight);

                    if(!PaletteManager.getIsEnabled()){
                        context.blit(PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, paletteItemHeight * 3, paletteItemWidth, paletteItemHeight);
                    }
                }

                // Draw selected hotbar slot
                ButtonInfo btnInfo = ButtonCatalogue.getSelectionButton(palette.getHotbarSlot() - 1);
                context.blit(btnInfo.identifier, xPos + paletteHotbarMarginX + ButtonCatalogue.smallButtonSize * (palette.getHotbarSlot() - 1), yPos + paletteHotbarMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize);

                context.drawString(this.font, Component.literal(palette.getShortenedName(this.font, maxPaletteTitleWidth)),
                        xPos + paletteItemTitleMarginX,
                        yPos + paletteItemTitleMarginY, 0x404040, false);

            }else{
                break;
            }
        }

        // Toggle Palette Enabled
        if(isPointInRegion(leftPos + paletteToggleEnabledMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            ButtonInfo btnInfo = ButtonCatalogue.getTogglePalettesHover();
            context.blit(btnInfo.identifier, leftPos + paletteToggleEnabledMarginX, topPos + paletteButtonMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize);
            context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.togglePalettes").toFlatList(), mouseX, mouseY);
        }  // Deselect all palettes
        else if(isPointInRegion(leftPos + paletteDeselectAllMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            ButtonInfo btnInfo = ButtonCatalogue.getDeselectAllHover();
            context.blit(btnInfo.identifier, leftPos + paletteDeselectAllMarginX, topPos + paletteButtonMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize);
            context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.deselectPalettes").toFlatList(), mouseX, mouseY);
        }  // Import palette
        else if(isPointInRegion(leftPos + paletteImportMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            ButtonInfo btnInfo = ButtonCatalogue.getImportHover();
            context.blit(btnInfo.identifier, leftPos + paletteImportMarginX, topPos + paletteButtonMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize);
            context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.importPalette").toFlatList(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        List<Palette> palettes = PaletteManager.getBuilderPalettes();
        final int scrollLevel = (int)scrollPosition;
        int xPos = leftPos + paletteContainerStartWidth;

        // Toggle Palette Enabled
        if(isPointInRegion(leftPos + paletteToggleEnabledMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            PaletteManager.toggleEnabled();
            return true;
        }  // Deselect all palettes
        else if(isPointInRegion(leftPos + paletteDeselectAllMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            PaletteManager.deselectAllPalettes();
            return true;
        }  // Import palette
        else if(isPointInRegion(leftPos + paletteImportMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            PaletteManager.importPalette();
            return true;
        }  // Clicked on scroller
        else if(isPointInRegion(leftPos + scrollMarginX, getCurrentScrollerYPosition(), scrollerWidth, scrollerHeight, (int)mouseX, (int)mouseY)) {
            clickedOnScroller = true;
            return true;
        }

        for (int paletteNo = scrollLevel; paletteNo < scrollLevel + 4; paletteNo++) {

            int slotNo = paletteNo - scrollLevel;
            int yPos = topPos + paletteContainerStartHeight + slotNo * paletteItemHeight;

            if(paletteNo == PaletteManager.getBuilderPalettes().size()){  // Add new element
                if(isPointInRegion(xPos, yPos, paletteItemWidth, paletteItemHeight, (int)mouseX, (int)mouseY)){
                    WeightCategory newWeightCat = new WeightCategory(100, new ArrayList<>());
                    Palette newPalette = new Palette("", new ItemStack(Items.GRASS_BLOCK), 9, new ArrayList<>(List.of(newWeightCat)));
                    palettes.add(newPalette);
                    client.setScreen(new PaletteEditScreen(client.player, newPalette));
                    return true;
                }
            }else if(paletteNo < PaletteManager.getBuilderPalettes().size()){  // List existing element
                Palette palette = palettes.get(paletteNo);

                // Enter edit mode
                if(isPointInRegion(xPos + paletteEditMarginX, yPos + paletteEditMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
                    client.setScreen(new PaletteEditScreen(client.player, palette));
                    return true;

                }  // Delete palette
                else if(isPointInRegion(xPos + paletteDeleteMarginX, yPos + paletteDeleteMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {

                    if (toBeDeletedId >= 0 && deleteConfirm > 0) {
                        PaletteManager.deletePalette(toBeDeletedId);
                        deleteConfirm = 0;
                        toBeDeletedId = -1;
                    } else {
                        this.toBeDeletedId = paletteNo;
                        this.deleteConfirm = DELETE_DOUBLE_CLICK_DURATION;
                        return true;
                    }
                }  // Select hotbar slot
                else if(isPointInRegion(xPos + paletteHotbarMarginX, yPos + paletteHotbarMarginY, ButtonCatalogue.smallButtonSize * 9, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {

                    int clickedHotbarSlot = ((int)mouseX - xPos - paletteHotbarMarginX) / ButtonCatalogue.smallButtonSize;
                    palette.setHotbarSlot(clickedHotbarSlot + 1);
                    return true;
                }
                // Select palette
                else if(isPointInRegion(xPos, yPos, paletteItemWidth, paletteItemHeight, (int)mouseX, (int)mouseY)) {

                    if(PaletteManager.addSelectedPalettes(palette)){
                        PaletteManager.setIsEnabled(true);
                    }
                    return true;
                }
            }else{
                break;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            client.setScreen(null);
            SaveHelper.saveSettings();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        if (button == 0 && clickedOnScroller){
            clickedOnScroller = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {

        if(clickedOnScroller) {
            double scrollStepHeight = scrollbarHeight / (double) PaletteManager.getBuilderPalettes().size();
            double relativeMousePosY = mouseY - topPos - scrollMarginY + (scrollStepHeight / 2);

            if (relativeMousePosY <= 0) {
                scrollPosition = 0;
            } else if (relativeMousePosY >= scrollbarHeight) {
                scrollPosition = PaletteManager.getBuilderPalettes().size();
            } else {
                scrollPosition = (relativeMousePosY) / scrollStepHeight;
            }
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollPosition -= verticalAmount;

        if(scrollPosition <= 0)
            scrollPosition = 0;
        else if(scrollPosition >= PaletteManager.getBuilderPalettes().size())
            scrollPosition = PaletteManager.getBuilderPalettes().size();

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void tick(){
        if(deleteConfirm > 0)
            deleteConfirm--;
    }

    private int getCurrentScrollerYPosition(){
        final int scrollLevel = (int)scrollPosition;

        return (int)(topPos + scrollMarginY + ((float)scrollLevel / PaletteManager.getBuilderPalettes().size() * (scrollbarHeight - scrollerHeight)));
    }

    private boolean isPointInRegion(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }
}
