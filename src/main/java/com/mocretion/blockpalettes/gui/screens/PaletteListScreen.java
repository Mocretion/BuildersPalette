package com.mocretion.blockpalettes.gui.screens;

import com.mocretion.blockpalettes.BlockPalettes;
import com.mocretion.blockpalettes.data.Palette;
import com.mocretion.blockpalettes.data.PaletteManager;
import com.mocretion.blockpalettes.data.WeightCategory;
import com.mocretion.blockpalettes.data.helper.SaveHelper;
import com.mocretion.blockpalettes.gui.ButtonCatalogue;
import com.mocretion.blockpalettes.gui.ButtonInfo;
import com.mocretion.blockpalettes.gui.draw.CustomDrawContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class PaletteListScreen extends Screen {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettes.MOD_ID, "textures/gui/palette_list.png");
    private static final ResourceLocation PALETTE_PREVIEW_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettes.MOD_ID, "textures/gui/palette_preview.png");
    private static final ResourceLocation ADD_PALETTE_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettes.MOD_ID, "textures/gui/add_palette.png");
    private static final ResourceLocation SCROLLER_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettes.MOD_ID, "textures/gui/scroller.png");
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
    private final int paletteSmallItemWidth = 54;

    private final int paletteItemTitleMarginX = 46;
    private final int paletteItemTitleMarginY = 7;
    private final int paletteSmallItemTitleMarginX = 22;
    private final int paletteSmallItemTitleMarginY = 7;

    private final int paletteItemIconMargin = 4;
    private final int paletteSmallItemIconMargin = 3;

    private final int paletteItemIconPreviewMarginX = 45;
    private final int paletteItemIconPreviewMarginY = 18;

    private final int paletteEditMarginX = 130;
    private final int paletteEditMarginY = 2;
    private final int paletteSmallEditMarginX = 37;
    private final int paletteSmallEditMarginY = 22;

    private final int paletteDeleteMarginX = 146;
    private final int paletteDeleteMarginY = 2;
    private final int paletteSmallDeleteMarginX = 37;
    private final int paletteSmallDeleteMarginY = 38;

    private final int paletteHotbarMarginX = 18;
    private final int paletteHotbarMarginY = 39;
    private final int paletteSmallHotbarMarginX = 2;
    private final int paletteSmallHotbarMarginY = 22;

    private final int paletteButtonMarginY = 238;
    private final int paletteToggleEnabledMarginX = 7;
    private final int paletteDeselectAllMarginX = 23;
    private final int paletteImportMarginX = 39;
    private final int paletteSearchMarginX = 57;
    private final int paletteSearchMarginY = 241;
    private final int paletteSearchWidth = 112;
    private final int paletteSearchHeight = 10;
    private final int paletteChangeLayoutMarginX = 176;
    private final int paletteChangeLayoutMarginY = 4;
    private final int itemSlotSize = 18;

    private final int maxPaletteTitleWidth = 80;
    private final int maxPaletteSmallTitleWidth = 40;

    private static final byte DELETE_DOUBLE_CLICK_DURATION = 20;
    private byte deleteConfirm;
    private int toBeDeletedId;

    private int leftPos;
    private int topPos;

    // Scrolling
    private double scrollPosition;
    private boolean clickedOnScroller;

    // Input fields
    private String searchText;
    private boolean isInputSelected;
    private byte selectedInputBlink;
    private static final byte SELECTED_INPUT_BLINK_DURATION = 10;
    private boolean markedEntireInput;
    private static final int MAX_TITLE_LENGTH = 18;

    private Minecraft client;

    public PaletteListScreen(Minecraft client) {
        super(Component.translatable("container.blockpalettes.palette_list"));
        this.client = client;
    }

    @Override
    protected void init() {
        super.init();
        searchText = "";
        this.leftPos = (this.width - this.backgroundWidth) / 2;
        this.topPos = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta){

        super.renderBackground(context, mouseX, mouseY, delta);

        // Draw background
        context.blit(RenderType::guiTextured, BG_TEXTURE, leftPos, topPos, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);

        // Draw title
        context.drawString(this.font, this.title,
                leftPos + (backgroundWidth - font.width(this.title)) / 2,
                topPos + 6, 0x404040, false);

        // Draw scroller
        final int scrollLevel = (int)scrollPosition;
        context.blit(RenderType::guiTextured, SCROLLER_TEXTURE, leftPos + scrollMarginX, getCurrentScrollerYPosition(), 0, 0, scrollerWidth, scrollerHeight, 256, 256);

        // Draw searchText
        String editedSearchText = searchText;

        if(isInputSelected && selectedInputBlink < SELECTED_INPUT_BLINK_DURATION)
            editedSearchText += "_";
        context.drawString(this.font, editedSearchText,
                leftPos + paletteSearchMarginX,
                topPos + paletteSearchMarginY,
                this.markedEntireInput ? 0x539de6 : 0xffffff, true);

        // Draw visual inventory slots
        List<Palette> palettes = PaletteManager.getBuilderPalettes(searchText);

        if(PaletteManager.isLargeMenu())
            renderLargeUI(context, mouseX, mouseY, palettes, scrollLevel);
        else
            renderSmallUI(context, mouseX, mouseY, palettes, scrollLevel);

        // Toggle Palette Enabled
        if(isPointInRegion(leftPos + paletteToggleEnabledMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            renderToggleHoverButton(context, mouseX, mouseY);
        }  // Deselect all palettes
        else if(isPointInRegion(leftPos + paletteDeselectAllMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            renderDeselectAllHoverButton(context, mouseX, mouseY);
        }  // Import palette
        else if(isPointInRegion(leftPos + paletteImportMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            renderImportHoverButton(context, mouseX, mouseY);
        }  // Searchbar
        else if(isPointInRegion(leftPos + paletteSearchMarginX, topPos + paletteSearchMarginY, paletteSearchWidth, paletteSearchHeight, (int)mouseX, (int)mouseY)){
            renderSearchbarTooltip(context, mouseX, mouseY);
        }  // Layout hover
        else if(isPointInRegion(leftPos + paletteChangeLayoutMarginX, topPos + paletteChangeLayoutMarginY, ButtonCatalogue.xsButtonSize, ButtonCatalogue.xsButtonSize, (int)mouseX, (int)mouseY)){
            renderLayoutToggleButton(context, mouseX, mouseY);
        }
    }

    private void renderLargeUI(GuiGraphics context, int mouseX, int mouseY, List<Palette> palettes, int scrollLevel){
        CustomDrawContext customDrawContext = new CustomDrawContext(client, context);
        int xPos = leftPos + paletteContainerStartWidth;
        for (int paletteNo = scrollLevel; paletteNo < scrollLevel + 4; paletteNo++) {

            int slotNo = paletteNo - scrollLevel;
            int yPos = topPos + paletteContainerStartHeight + slotNo * paletteItemHeight;

            if(paletteNo == palettes.size()){  // Add new element
                context.blit(RenderType::guiTextured, ADD_PALETTE_TEXTURE, xPos, yPos, 0, 0, paletteItemWidth, paletteItemHeight, 256, 256);

                if(isPointInRegion(xPos, yPos, paletteItemWidth, paletteItemHeight, mouseX, mouseY)){
                    context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.addPalette").toFlatList(), mouseX, mouseY);
                }

            }else if(paletteNo < palettes.size()){  // List existing element
                Palette palette = palettes.get(paletteNo);
                context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, 0, paletteItemWidth, paletteItemHeight, 256, 256);

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
                    renderEditHoverButton(context, mouseX, mouseY, xPos + paletteEditMarginX, yPos + paletteEditMarginY, xPos, yPos);
                }  // Draw delete confirmation
                else if(this.deleteConfirm > 0 && this.toBeDeletedId == paletteNo){
                    renderDeleteConfirmButton(context, mouseX, mouseY,xPos + paletteDeleteMarginX, yPos + paletteDeleteMarginY, xPos, yPos);
                }  // Draw delete hover
                else if(isPointInRegion(xPos + paletteDeleteMarginX, yPos + paletteDeleteMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {
                    renderDeleteHoverButton(context, mouseX, mouseY,xPos + paletteDeleteMarginX, yPos + paletteDeleteMarginY, xPos, yPos);
                }  // Show selection toolbar slot
                else if(isPointInRegion(xPos + paletteHotbarMarginX, yPos + paletteHotbarMarginY, ButtonCatalogue.smallButtonSize * 9, ButtonCatalogue.smallButtonSize, mouseX, mouseY)){
                    renderHotbarTooltip(context, mouseX, mouseY);
                }
                // Draw hover texture
                else if(isPointInRegion(xPos, yPos, paletteItemWidth, paletteItemHeight, mouseX, mouseY)){
                    renderPaletteHoverButton(context, mouseX, mouseY, xPos, yPos, palette);
                }

                // Draw selected texture
                if(PaletteManager.isPaletteSelected(palette)){
                    context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, paletteItemHeight * 2, paletteItemWidth, paletteItemHeight, 256, 256);

                    if(!PaletteManager.getIsEnabled()){
                        context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, xPos, yPos, 0, paletteItemHeight * 3, paletteItemWidth, paletteItemHeight, 256, 256);
                    }
                }

                // Draw selected hotbar slot
                ButtonInfo btnInfo = ButtonCatalogue.getSelectionButton(palette.getHotbarSlot() - 1);
                context.blit(RenderType::guiTextured, btnInfo.identifier, xPos + paletteHotbarMarginX + ButtonCatalogue.smallButtonSize * (palette.getHotbarSlot() - 1), yPos + paletteHotbarMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, 256, 256);

                context.drawString(this.font, Component.literal(palette.getShortenedName(this.font, maxPaletteTitleWidth)),
                        xPos + paletteItemTitleMarginX,
                        yPos + paletteItemTitleMarginY, 0x404040, false);

            }else{
                break;
            }
        }
    }
    private void renderSmallUI(GuiGraphics context, int mouseX, int mouseY, List<Palette> palettes, int scrollLevel) {
        for (int rowNo = scrollLevel; rowNo < scrollLevel + 4; rowNo++) {

            int yPos = topPos + paletteContainerStartHeight + rowNo * paletteItemHeight;

            for (int columnNo = 0; columnNo < 3; columnNo++) {

                int paletteNo = scrollLevel * 3 + rowNo * 3 + columnNo;
                int xPos = leftPos + paletteContainerStartWidth + paletteSmallItemWidth * columnNo;

                if (paletteNo == palettes.size()) {  // Add new element
                    context.blit(RenderType::guiTextured, ADD_PALETTE_TEXTURE, xPos, yPos, 202, 0, paletteSmallItemWidth, paletteItemHeight, 256, 256);

                    if (isPointInRegion(xPos, yPos, paletteSmallItemWidth, paletteItemHeight, mouseX, mouseY)) {
                        context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.addPalette").toFlatList(), mouseX, mouseY);
                    }

                } else if (paletteNo < palettes.size()) {  // List existing element
                    Palette palette = palettes.get(paletteNo);
                    context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, xPos, yPos, 202, 0, paletteSmallItemWidth, paletteItemHeight, 256, 256);

                    // Draw icon
                    context.renderItem(palette.getIcon(), xPos + paletteSmallItemIconMargin, yPos + paletteSmallItemIconMargin);

                    // Draw edit hover
                    if (isPointInRegion(xPos + paletteSmallEditMarginX, yPos + paletteSmallEditMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int) mouseX, (int) mouseY)) {
                        renderEditHoverButton(context, mouseX, mouseY, xPos + paletteSmallEditMarginX, yPos + paletteSmallEditMarginY, xPos, yPos);
                    }  // Draw delete confirmation
                    else if (this.deleteConfirm > 0 && this.toBeDeletedId == paletteNo) {
                        renderDeleteConfirmButton(context, mouseX, mouseY, xPos + paletteSmallDeleteMarginX, yPos + paletteSmallDeleteMarginX, xPos, yPos);
                    }  // Draw delete hover
                    else if (isPointInRegion(xPos + paletteSmallDeleteMarginX, yPos + paletteSmallDeleteMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int) mouseX, (int) mouseY)) {
                        renderDeleteHoverButton(context, mouseX, mouseY, xPos + paletteSmallDeleteMarginX, yPos + paletteSmallDeleteMarginY, xPos, yPos);
                    }  // Show selection toolbar slot
                    else if (isPointInRegion(xPos + paletteSmallHotbarMarginX, yPos + paletteSmallHotbarMarginY, ButtonCatalogue.xsButtonSize * 3, ButtonCatalogue.xsButtonSize * 3, mouseX, mouseY)) {
                        renderHotbarTooltip(context, mouseX, mouseY);
                    }
                    // Draw hover texture
                    else if (isPointInRegion(xPos, yPos, paletteSmallItemWidth, paletteItemHeight, mouseX, mouseY)) {
                        renderPaletteHoverButton(context, mouseX, mouseY, xPos, yPos, palette);
                    }

                    // Draw selected texture
                    if (PaletteManager.isPaletteSelected(palette)) {
                        context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, xPos, yPos, 202, paletteItemHeight * 2, paletteSmallItemWidth, paletteItemHeight, 256, 256);

                        if (!PaletteManager.getIsEnabled()) {
                            context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, xPos, yPos, 202, paletteItemHeight * 3, paletteSmallItemWidth, paletteItemHeight, 256, 256);
                        }
                    }

                    // Draw selected hotbar slot
                    ButtonInfo btnInfo = ButtonCatalogue.getSelectionButtonXs(palette.getHotbarSlot() - 1);
                    context.blit(RenderType::guiTextured, btnInfo.identifier, xPos + paletteSmallHotbarMarginX + ButtonCatalogue.xsButtonSize * ((palette.getHotbarSlot() - 1) % 3), yPos + paletteSmallHotbarMarginY + (palette.getHotbarSlot() - 1) / 3 * ButtonCatalogue.xsButtonSize, btnInfo.u, btnInfo.v, ButtonCatalogue.xsButtonSize, ButtonCatalogue.xsButtonSize, 256, 256);

                    context.drawString(this.font, Component.literal(palette.getShortenedName(this.font, maxPaletteSmallTitleWidth)),
                            xPos + paletteSmallItemTitleMarginX,
                            yPos + paletteSmallItemTitleMarginY, 0x404040, false);

                } else {
                    break;
                }
            }
        }
    }

    private void renderEditHoverButton(GuiGraphics context, int mouseX, int mouseY, int posXEdit, int posYEdit, int posXBg, int posYBg){
        ButtonInfo btnInfo = ButtonCatalogue.getEditHover();
        context.blit(RenderType::guiTextured, btnInfo.identifier, posXEdit, posYEdit, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, 256, 256);
        context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, posXBg, posYBg, PaletteManager.isLargeMenu() ? 0 : 202, paletteItemHeight, PaletteManager.isLargeMenu() ? paletteItemWidth : paletteSmallItemWidth, paletteItemHeight, 256, 256);
        context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.editPalette").toFlatList(), mouseX, mouseY);
    }

    private void renderDeleteConfirmButton(GuiGraphics context, int mouseX, int mouseY, int posXDel, int posYDel, int posXBg, int posYBg){
        ButtonInfo btnInfo = ButtonCatalogue.getDeleteConfirm();
        context.blit(RenderType::guiTextured, btnInfo.identifier, posXDel, posYDel + 1, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, 256, 256);
        context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, posXBg, posYBg, PaletteManager.isLargeMenu() ? 0 : 202, paletteItemHeight, PaletteManager.isLargeMenu() ? paletteItemWidth : paletteSmallItemWidth, paletteItemHeight, 256, 256);
        context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.deletePalette").toFlatList(), mouseX, mouseY);
    }

    public void renderDeleteHoverButton(GuiGraphics context, int mouseX, int mouseY, int posXDel, int posYDel, int posXBg, int posYBg){
        ButtonInfo btnInfo = ButtonCatalogue.getDeleteHover();
        context.blit(RenderType::guiTextured, btnInfo.identifier, posXDel, posYDel, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, 256, 256);
        context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, posXBg, posYBg, PaletteManager.isLargeMenu() ? 0 : 202, paletteItemHeight, PaletteManager.isLargeMenu() ? paletteItemWidth : paletteSmallItemWidth, paletteItemHeight, 256, 256);
        context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.deletePalette").toFlatList(), mouseX, mouseY);
    }

    public void renderHotbarTooltip(GuiGraphics context, int mouseX, int mouseY){
        context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.selectHotbarSlot").toFlatList(), mouseX, mouseY);
    }

    public void renderPaletteHoverButton(GuiGraphics context, int mouseX, int mouseY, int posXBg, int posYBg, Palette palette){
        context.blit(RenderType::guiTextured, PALETTE_PREVIEW_TEXTURE, posXBg, posYBg, PaletteManager.isLargeMenu() ? 0 : 202, paletteItemHeight, PaletteManager.isLargeMenu() ? paletteItemWidth : paletteSmallItemWidth, paletteItemHeight, 256, 256);

        List<FormattedCharSequence> paletteNameTooltip = new ArrayList<>();
        paletteNameTooltip.add(Component.literal(palette.getName()).getVisualOrderText());
        paletteNameTooltip.add(Component.translatable("container.blockpalettes.paletteIcon").append("§8" + palette.getIconName()).getVisualOrderText());
        context.renderTooltip(this.font, paletteNameTooltip, mouseX, mouseY);
    }

    private void renderToggleHoverButton(GuiGraphics context, int mouseX, int mouseY){
        ButtonInfo btnInfo = ButtonCatalogue.getTogglePalettesHover();
        context.blit(RenderType::guiTextured, btnInfo.identifier, leftPos + paletteToggleEnabledMarginX, topPos + paletteButtonMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, 256, 256);

        List<FormattedCharSequence> togglePaletteTooltip = new ArrayList<>();
        togglePaletteTooltip.add(Component.translatable("container.blockpalettes.togglePalettes").getVisualOrderText());
        togglePaletteTooltip.add(Component.empty().getVisualOrderText());
        togglePaletteTooltip.add(Component.translatable("container.blockpalettes.currentState").append(PaletteManager.getIsEnabled() ? Component.translatable("container.blockpalettes.enabled") : Component.translatable("container.blockpalettes.disabled")).getVisualOrderText());

        context.renderTooltip(this.font, togglePaletteTooltip, mouseX, mouseY);
    }

    private void renderDeselectAllHoverButton(GuiGraphics context, int mouseX, int mouseY){
        ButtonInfo btnInfo = ButtonCatalogue.getDeselectAllHover();
        context.blit(RenderType::guiTextured, btnInfo.identifier, leftPos + paletteDeselectAllMarginX, topPos + paletteButtonMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, 256, 256);
        context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.deselectPalettes").toFlatList(), mouseX, mouseY);
    }

    private void renderImportHoverButton(GuiGraphics context, int mouseX, int mouseY){
        ButtonInfo btnInfo = ButtonCatalogue.getImportHover();
        context.blit(RenderType::guiTextured, btnInfo.identifier, leftPos + paletteImportMarginX, topPos + paletteButtonMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, 256, 256);
        context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.importPalette").toFlatList(), mouseX, mouseY);
    }

    private void renderSearchbarTooltip(GuiGraphics context, int mouseX, int mouseY){
        List<FormattedCharSequence> searchbarTooltip = new ArrayList<>();
        searchbarTooltip.add(Component.translatable("container.blockpalettes.filterPalettesByName").getVisualOrderText());
        searchbarTooltip.add(Component.empty().getVisualOrderText());
        searchbarTooltip.add(Component.translatable("container.blockpalettes.filterPalettesByNameInfo").getVisualOrderText());

        context.renderTooltip(this.font, searchbarTooltip, mouseX, mouseY);
    }

    private void renderLayoutToggleButton(GuiGraphics context, int mouseX, int mouseY){
        ButtonInfo btnInfo = ButtonCatalogue.getToggleLayoutHover();
        context.blit(RenderType::guiTextured, btnInfo.identifier, leftPos + paletteChangeLayoutMarginX, topPos + paletteChangeLayoutMarginY, btnInfo.u, btnInfo.v, ButtonCatalogue.xsButtonSize, ButtonCatalogue.xsButtonSize, 256, 256);
        context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.toggleLayout").toFlatList(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        isInputSelected = false;
        selectedInputBlink = 0;
        markedEntireInput = false;

        // Toggle Palette Enabled
        if(isPointInRegion(leftPos + paletteToggleEnabledMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            PaletteManager.toggleEnabled();
            playButtonClickSound();
            return true;
        }  // Deselect all palettes
        else if(isPointInRegion(leftPos + paletteDeselectAllMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            PaletteManager.deselectAllPalettes();
            playButtonClickSound();
            return true;
        }  // Import palette
        else if(isPointInRegion(leftPos + paletteImportMarginX, topPos + paletteButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
            PaletteManager.importPalette();
            playButtonClickSound();
            return true;
        }  // Clicked on scroller
        else if(isPointInRegion(leftPos + scrollMarginX, getCurrentScrollerYPosition(), scrollerWidth, scrollerHeight, (int)mouseX, (int)mouseY)) {
            clickedOnScroller = true;
            return true;
        }  // Clicked on searchbar
        else if(isPointInRegion(leftPos + paletteSearchMarginX, topPos + paletteSearchMarginY, paletteSearchWidth, paletteSearchHeight, (int)mouseX, (int)mouseY)){
            isInputSelected = true;
            selectedInputBlink = 0;
            markedEntireInput = false;
            return true;
        }  // Clicked on layout toggle
        else if(isPointInRegion(leftPos + paletteChangeLayoutMarginX, topPos + paletteChangeLayoutMarginY, ButtonCatalogue.xsButtonSize, ButtonCatalogue.xsButtonSize, (int)mouseX, (int)mouseY)){
            PaletteManager.toggleLayout();
            playButtonClickSound();
            return true;
        }

        if(PaletteManager.isLargeMenu()) {
            if (mouseClickLarge(mouseX, mouseY)) {
                return true;
            }
        }
        else {
            if (mouseClickSmall(mouseX, mouseY)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseClickLarge(double mouseX, double mouseY){

        List<Palette> palettes = PaletteManager.getBuilderPalettes(this.searchText);
        final int scrollLevel = (int)scrollPosition;
        int xPos = leftPos + paletteContainerStartWidth;

        for (int paletteNo = scrollLevel; paletteNo < scrollLevel + 4; paletteNo++) {

            int startSlotNo = paletteNo - scrollLevel;
            int yPos = topPos + paletteContainerStartHeight + startSlotNo * paletteItemHeight;

            if(paletteNo == palettes.size()){  // Add new element
                if(isPointInRegion(xPos, yPos, paletteItemWidth, paletteItemHeight, (int)mouseX, (int)mouseY)){
                    WeightCategory newWeightCat = new WeightCategory(100, new ArrayList<>());
                    Palette newPalette = new Palette("", new ItemStack(Items.GRASS_BLOCK), 9, new ArrayList<>(List.of(newWeightCat)));
                    PaletteManager.getBuilderPalettes().add(newPalette);
                    client.setScreen(new PaletteEditScreen(client.player, newPalette));
                    playButtonClickSound();
                    return true;
                }
            }else if(paletteNo < palettes.size()){  // List existing element
                Palette palette = palettes.get(paletteNo);

                // Enter edit mode
                if(isPointInRegion(xPos + paletteEditMarginX, yPos + paletteEditMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
                    client.setScreen(new PaletteEditScreen(client.player, palette));
                    playButtonClickSound();
                    return true;

                }  // Delete palette
                else if(isPointInRegion(xPos + paletteDeleteMarginX, yPos + paletteDeleteMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {

                    playButtonClickSound();
                    if (toBeDeletedId == paletteNo && deleteConfirm > 0) {
                        PaletteManager.deletePalette(toBeDeletedId);
                        deleteConfirm = 0;
                        toBeDeletedId = -1;
                    } else {
                        this.toBeDeletedId = paletteNo;
                        this.deleteConfirm = DELETE_DOUBLE_CLICK_DURATION;
                    }
                    return true;

                }  // Select hotbar slot
                else if(isPointInRegion(xPos + paletteHotbarMarginX, yPos + paletteHotbarMarginY, ButtonCatalogue.smallButtonSize * 9, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {

                    int clickedHotbarSlot = ((int)mouseX - xPos - paletteHotbarMarginX) / ButtonCatalogue.smallButtonSize;
                    selectHotbarSlot(palette, clickedHotbarSlot + 1);
                    return true;
                }
                // Select palette
                else if(isPointInRegion(xPos, yPos, paletteItemWidth, paletteItemHeight, (int)mouseX, (int)mouseY)) {

                    if(PaletteManager.addSelectedPalettes(palette)){
                        PaletteManager.setIsEnabled(true);
                    }

                    setPaletteItemIfEnabled(palette);
                    playButtonClickSound();

                    return true;
                }
            }else{
                break;
            }
        }

        return false;
    }

    public boolean mouseClickSmall(double mouseX, double mouseY){

        List<Palette> palettes = PaletteManager.getBuilderPalettes(this.searchText);
        final int scrollLevel = (int)scrollPosition;

        for (int rowNo = scrollLevel; rowNo < scrollLevel + 4; rowNo++) {

            int yPos = topPos + paletteContainerStartHeight + rowNo * paletteItemHeight;

            for (int columnNo = 0; columnNo < 3; columnNo++) {

                int paletteNo = scrollLevel * 3 + rowNo * 3 + columnNo;
                int xPos = leftPos + paletteContainerStartWidth + paletteSmallItemWidth * columnNo;

                if (paletteNo == palettes.size()) {  // Add new element
                    if(isPointInRegion(xPos, yPos, paletteSmallItemWidth, paletteItemHeight, (int)mouseX, (int)mouseY)) {
                        WeightCategory newWeightCat = new WeightCategory(100, new ArrayList<>());
                        Palette newPalette = new Palette("", new ItemStack(Items.GRASS_BLOCK), 9, new ArrayList<>(List.of(newWeightCat)));
                        PaletteManager.getBuilderPalettes().add(newPalette);
                        client.setScreen(new PaletteEditScreen(client.player, newPalette));
                        playButtonClickSound();
                        return true;
                    }

                } else if (paletteNo < palettes.size()) {  // List existing element
                    Palette palette = palettes.get(paletteNo);
                    // Enter edit mode
                    if(isPointInRegion(xPos + paletteSmallEditMarginX, yPos + paletteSmallEditMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)){
                        client.setScreen(new PaletteEditScreen(client.player, palette));
                        playButtonClickSound();
                        return true;

                    }  // Delete palette
                    else if(isPointInRegion(xPos + paletteSmallDeleteMarginX, yPos + paletteSmallDeleteMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {

                        playButtonClickSound();
                        if (toBeDeletedId == paletteNo && deleteConfirm > 0) {
                            PaletteManager.deletePalette(toBeDeletedId);
                            deleteConfirm = 0;
                            toBeDeletedId = -1;
                        } else {
                            this.toBeDeletedId = paletteNo;
                            this.deleteConfirm = DELETE_DOUBLE_CLICK_DURATION;
                        }
                        return true;

                    }  // Select hotbar slot
                    else if(isPointInRegion(xPos + paletteSmallHotbarMarginX, yPos + paletteSmallHotbarMarginY, ButtonCatalogue.xsButtonSize * 3, ButtonCatalogue.xsButtonSize * 3, (int)mouseX, (int)mouseY)) {

                        int clickedHotbarSlot = (((int)mouseX - xPos - paletteSmallHotbarMarginX) / ButtonCatalogue.xsButtonSize) + (3 * (((int)mouseY - yPos - paletteSmallHotbarMarginY) / ButtonCatalogue.xsButtonSize));
                        selectHotbarSlot(palette, clickedHotbarSlot + 1);
                        return true;
                    }
                    // Select palette
                    else if(isPointInRegion(xPos, yPos, paletteSmallItemWidth, paletteItemHeight, (int)mouseX, (int)mouseY)) {

                        if(PaletteManager.addSelectedPalettes(palette)){
                            PaletteManager.setIsEnabled(true);
                        }
                        setPaletteItemIfEnabled(palette);
                        playButtonClickSound();

                        return true;
                    }

                } else {
                    break;
                }
            }
        }

        return false;
    }

    private void selectHotbarSlot(Palette palette, int clickedHotbarSlot){
        palette.setHotbarSlot(clickedHotbarSlot);
        setPaletteItemIfEnabled(palette);

        playButtonClickSound();
    }

    private void setPaletteItemIfEnabled(Palette palette){
        if(PaletteManager.getIsEnabled() && PaletteManager.isPaletteSelected(palette)){
            palette.getPaletteItemFromInventory(client.player);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            client.setScreen(null);
            SaveHelper.saveSettings();
            return true;
        }

        if(isInputSelected){
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.searchText.isEmpty()) {

                if (!markedEntireInput)// Remove the last character
                    searchText = searchText.substring(0, searchText.length() - 1);
                else {
                    searchText = "";
                    markedEntireInput = false;
                }
                return true;
            } else if (modifiers == 2 && keyCode == GLFW.GLFW_KEY_A) {
                markedEntireInput = true;
                return true;
            }
        }

        if(keyCode == GLFW.GLFW_KEY_E){
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char ch, int modifiers) {

        if (isInputSelected) {

            if (this.markedEntireInput) {
                searchText = ch + "";
                this.markedEntireInput = false;
                return true;
            } else {

                if(searchText.length() == MAX_TITLE_LENGTH)
                    return false;

                searchText += ch;
                return true;
            }

        }

        return super.charTyped(ch, modifiers);
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
            double scrollStepHeight = scrollbarHeight / (double) getMaxScrollSize();
            double relativeMousePosY = mouseY - topPos - scrollMarginY + (scrollStepHeight / 2);

            if (relativeMousePosY <= 0) {
                scrollPosition = 0;
            } else if (relativeMousePosY >= scrollbarHeight) {
                scrollPosition = getMaxScrollSize();
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
        else if(scrollPosition >= getMaxScrollSize())
            scrollPosition = getMaxScrollSize();

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private int getMaxScrollSize(){
        if(PaletteManager.isLargeMenu())
            return PaletteManager.getBuilderPalettes(this.searchText).size();

        return (int) (Math.ceil(PaletteManager.getBuilderPalettes(this.searchText).size() + 1) / 3f);
    }

    @Override
    public void tick(){
        if(deleteConfirm > 0)
            deleteConfirm--;

        selectedInputBlink++;

        if(selectedInputBlink >= SELECTED_INPUT_BLINK_DURATION * 2)
            selectedInputBlink = 0;
    }

    private int getCurrentScrollerYPosition(){
        final int scrollLevel = (int)scrollPosition;

        if(PaletteManager.getBuilderPalettes(this.searchText).isEmpty()){
            return topPos + scrollMarginY;
        }

        return (int)(topPos + scrollMarginY + ((float)scrollLevel / PaletteManager.getBuilderPalettes().size() * (scrollbarHeight - scrollerHeight)));
    }

    private boolean isPointInRegion(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }

    public static void playButtonClickSound() {
        Minecraft client = Minecraft.getInstance();
        client.getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
        );
    }
}