package com.mocretion.blockpalettes.gui.screens;

import com.mocretion.blockpalettes.BlockPalettesClient;
import com.mocretion.blockpalettes.data.Palette;
import com.mocretion.blockpalettes.data.WeightCategory;
import com.mocretion.blockpalettes.data.helper.SaveHelper;
import com.mocretion.blockpalettes.gui.ButtonCatalogue;
import com.mocretion.blockpalettes.gui.ButtonInfo;
import com.mocretion.blockpalettes.gui.screens.menutypes.EditMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * I know this is a mess
 */
public class PaletteEditScreen extends AbstractContainerScreen<EditMenu> implements MenuAccess<EditMenu> {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/palette_view.png");
    private static final ResourceLocation ADD_WEIGHT_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/add_row.png");
    private static final ResourceLocation INVENTORY_ROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/inventory_row.png");
    private static final ResourceLocation TEXT_ROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/text_row.png");
    private static final ResourceLocation SCROLLER_TEXTURE = ResourceLocation.fromNamespaceAndPath(BlockPalettesClient.MOD_ID, "textures/gui/scroller.png");

    private static final int MAX_WEIGHT_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 21;

    private final Player player;
    private final Palette palette;

    // GUI dimensions
    private final int backgroundWidth = 195;
    private final int backgroundHeight = 256;

    private final int playerInvStartHeight = 174;
    private final int playerHotbarStartHeight = 232;
    private final int playerInvStartWidth = 8;


    private final int iconMarginX = 8;
    private final int iconMarginY = 6;
    private final int titleInputMarginX = 29;
    private final int titleInputMarginY = 12;
    private final int scrollMarginX = 175;
    private final int scrollMarginY = 26;
    private final int scrollerWidth = 12;
    private final int scrollerHeight = 15;
    private final int scrollbarHeight = 142;
    private final int titleInputWidth = 131;
    private final int titleInputHeight = 12;
    private final int weightContainerStartHeight = 25;
    private final int weightContainerStartWidth = 7;
    private final int weightItemsHeight = 18;
    private final int weightItemsWidth = 162;
    private final int weightTextInputMarginX = 4;
    private final int weightTextInputMarginY = 6;
    private final int weightTextInputWidth = 110;
    private final int weightTextInputHeight = 12;
    private final int deleteButtonMarginX = 146;
    private final int deleteButtonMarginY = 2;
    private final int exportButtonMarginX = 178;
    private final int exportButtonMarginY = 178;
    private final int buttonSize = 14;

    private final int itemSlotSize = 18;

    private int leftPos;
    private int topPos;

    // Slot tracking
    private ItemStack draggedStack = ItemStack.EMPTY;

    // Scrolling
    private double scrollPosition;
    private boolean clickedOnScroller;

    // Input fields
    private int selectedInput;
    private byte selectedInputBlink;
    private static final byte SELECTED_INPUT_BLINK_DURATION = 10;
    private boolean markedEntireInput;

    // Deletion
    private static final byte DELETE_DOUBLE_CLICK_DURATION = 20;
    private byte deleteConfirm;
    private int toBeDeletedId;

    public PaletteEditScreen(Player player, Palette palette) {
        super(new EditMenu(0, player.getInventory()), player.getInventory(), Component.translatable("container.blockpalettes.editTitle"));
        this.player = player;
        this.palette = palette;
        this.selectedInputBlink = 0;
        this.markedEntireInput = false;
    }

    public PaletteEditScreen(EditMenu handler, Inventory playerInv, Component title) {
        super(handler, playerInv, title);
        this.player = playerInv.player;
        this.palette = null;
        this.selectedInputBlink = 0;
        this.markedEntireInput = false;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.backgroundWidth) / 2;
        this.topPos = (this.height - this.backgroundHeight) / 2;
        this.selectedInput = -2;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {

    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta){

        super.renderBackground(context, mouseX, mouseY, delta);

        // Draw background
        context.blit(RenderType::guiTextured, BG_TEXTURE, leftPos, topPos, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);

        //renderBackground(context, mouseX, mouseY, delta);

        // Draw title
        String titleText = palette.getName();
        // Add blinking "_" in title field when selected
        if(selectedInput == -1){

            if(this.selectedInputBlink < SELECTED_INPUT_BLINK_DURATION){
                titleText += "_";
            }
        }

        context.drawString(this.font, titleText,
                leftPos + titleInputMarginX,
                topPos + titleInputMarginY, this.markedEntireInput && this.selectedInput == -1 ? 0x539de6 : 0xffffff, true);

        if(isPointInRegion(leftPos + titleInputMarginX, topPos + titleInputMarginY, titleInputWidth, titleInputHeight, (int) mouseX, (int) mouseY)) {
            context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.editTitle").toFlatList(), mouseX, mouseY);
        }

        // Draw Upper Inventory
        List<WeightCategory> weights = palette.getWeights();

        // Draw icon
        if(isPointInRegion(leftPos + iconMarginX, topPos + iconMarginY, itemSlotSize, itemSlotSize, (int) mouseX, (int) mouseY)) {
            context.fill(leftPos + iconMarginX, topPos + iconMarginY, leftPos + iconMarginX + 16, topPos + iconMarginY + 16, 0x80FFFFFF);
            context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.icon").toFlatList(), mouseX, mouseY);
        }
        context.renderItem(palette.getIcon(), leftPos + iconMarginX, topPos + iconMarginY);

        // Draw export hover
        if(isPointInRegion(leftPos + exportButtonMarginX, topPos + exportButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {
            ButtonInfo texture = ButtonCatalogue.getExportHover();
            context.blit(RenderType::guiTextured, texture.identifier, leftPos + exportButtonMarginX, topPos + exportButtonMarginY, texture.u, texture.v, buttonSize, buttonSize, 256, 256);
            context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.exportPalette").toFlatList(), mouseX, mouseY);
        }

        final int scrollLevel = (int)scrollPosition;

        // Draw scroller
        context.blit(RenderType::guiTextured, SCROLLER_TEXTURE, leftPos + scrollMarginX, getCurrentScrollerYPosition(), 0, 0, scrollerWidth, scrollerHeight, 256, 256);

        int xPos = leftPos + weightContainerStartWidth;
        boolean addedAddBtn = false;
        for (int weightSlot = scrollLevel; weightSlot < scrollLevel + 8; weightSlot++) {
            WeightRowInfo rowInfo = getRowsWeightInfo(weightSlot, weights);

            int yPos = topPos + weightContainerStartHeight + (weightSlot - scrollLevel) * weightItemsHeight;

            if(rowInfo.isBlank() && !addedAddBtn){  // Add new element

                context.blit(RenderType::guiTextured, ADD_WEIGHT_TEXTURE, xPos, yPos, 0, 0, weightItemsWidth, weightItemsHeight, 256, 256);

                if(isPointInRegion(xPos, yPos, weightItemsWidth, weightItemsHeight, (int) mouseX, (int) mouseY)) {
                    context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.addWeight").toFlatList(), mouseX, mouseY);
                }

                addedAddBtn = true;

            }else if(rowInfo.isWeightRow()){  // Add weight header

                context.blit(RenderType::guiTextured, TEXT_ROW_TEXTURE, xPos, yPos, 0, 0, weightItemsWidth, weightItemsHeight, 256, 256);

                String weightText = weights.get(rowInfo.weightCategoryId).getWeightInputField();

                // Add blinking "_" in input field when selected
                if(selectedInput == rowInfo.weightCategoryId){

                    if(this.selectedInputBlink < SELECTED_INPUT_BLINK_DURATION){
                        weightText += "_";
                    }
                }

                context.drawString(this.font, weightText,
                        xPos + weightTextInputMarginX,
                        yPos + weightTextInputMarginY,
                        this.markedEntireInput && this.selectedInput == rowInfo.weightCategoryId ? 0x539de6 : 0xffffff, true);

                if(isPointInRegion(xPos + weightTextInputMarginX, yPos + weightTextInputMarginY, weightTextInputWidth, weightTextInputHeight, (int) mouseX, (int) mouseY)) {
                    List<FormattedCharSequence> weightEditTooltip = new ArrayList<>();
                    weightEditTooltip.add(Component.translatable("container.blockpalettes.editWeight").getVisualOrderText());
                    weightEditTooltip.add(Component.empty().getVisualOrderText());

                    if(isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)){
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample1").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample2").getVisualOrderText());
                        weightEditTooltip.add(Component.empty().getVisualOrderText());

                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample3").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample4").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample5").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample6").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample7").getVisualOrderText());
                        weightEditTooltip.add(Component.empty().getVisualOrderText());

                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample8").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample9").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample10").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample11").getVisualOrderText());
                        weightEditTooltip.add(Component.empty().getVisualOrderText());

                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample12").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample13").getVisualOrderText());
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.weightExample14").getVisualOrderText());
                    }else{
                        weightEditTooltip.add(Component.translatable("container.blockpalettes.moreDetails").getVisualOrderText());
                    }

                    context.renderTooltip(this.font, weightEditTooltip, mouseX, mouseY);
                }

                // Is hovering over delete
                if(isPointInRegion(xPos + deleteButtonMarginX, yPos + deleteButtonMarginY, buttonSize, buttonSize, (int) mouseX, (int) mouseY)) {
                    ButtonInfo texture = ButtonCatalogue.getDeleteHover();
                    context.blit(RenderType::guiTextured, texture.identifier, xPos + deleteButtonMarginX, yPos + deleteButtonMarginY, texture.u, texture.v, buttonSize, buttonSize, 256, 256);
                    context.renderComponentTooltip(this.font, Component.translatable("container.blockpalettes.deleteWeight").toFlatList(), mouseX, mouseY);
                }

                // Is selected as delete
                if(this.deleteConfirm > 0 && this.toBeDeletedId == rowInfo.weightCategoryId){
                    ButtonInfo texture = ButtonCatalogue.getDeleteConfirm();
                    context.blit(RenderType::guiTextured, texture.identifier, xPos + deleteButtonMarginX, yPos + deleteButtonMarginY, texture.u, texture.v, buttonSize, buttonSize, 256, 256);
                }

                }else if(!rowInfo.isBlank()){  // Add inventory

                context.blit(RenderType::guiTextured, INVENTORY_ROW_TEXTURE, xPos, yPos, 0, 0, weightItemsWidth, weightItemsHeight, 256, 256);

                // Draw items
                for (int itemNo = 0; itemNo < rowInfo.items.size(); itemNo++) {

                    ItemStack item = rowInfo.items.get(itemNo);
                    context.renderItem(item, xPos + itemNo * itemSlotSize + 1, yPos + 1);
                }

                // Draw tooltip and hover effect
                if(isPointInRegion(xPos, yPos, weightItemsWidth, weightItemsHeight, mouseX, mouseY)){

                    int paletteRowSlot = getPaletteSlotAt(mouseX, mouseY);
                    context.fill(xPos + paletteRowSlot * itemSlotSize + 1, yPos + 1, xPos + paletteRowSlot * itemSlotSize + 17, yPos + 17, 0x80FFFFFF);

                    if (draggedStack.isEmpty()) {
                        if(paletteRowSlot < rowInfo.items.size()) {
                            ItemStack hoveredItem = rowInfo.items.get(paletteRowSlot);

                            // Item info tooltip
                            if (!hoveredItem.isEmpty()) {
                                context.renderComponentTooltip(this.font, getTooltipFromItem(this.minecraft, hoveredItem), mouseX, mouseY);
                            }
                        }else{
                            List<FormattedCharSequence> addItemsTooltip = new ArrayList<>();
                            addItemsTooltip.add(Component.translatable("container.blockpalettes.addItemToWeight").getVisualOrderText());
                            addItemsTooltip.add(Component.empty().getVisualOrderText());

                            if(isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                                addItemsTooltip.add(Component.translatable("container.blockpalettes.addItemToWeightJEIInfo").getVisualOrderText());

                            }else{
                                addItemsTooltip.add(Component.translatable("container.blockpalettes.moreDetails").getVisualOrderText());
                            }

                            context.renderTooltip(this.font, addItemsTooltip, mouseX, mouseY);

                        }
                    }
                }
            }
        }

        int hoveredSlot = getPlayerSlotAt(mouseX, mouseY);
        if(hoveredSlot >= 0){
            if(hoveredSlot < 9){ // Hotbar
                int slotX = leftPos + 8 + hoveredSlot * itemSlotSize;
                int slotY = topPos + playerHotbarStartHeight;
                context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80FFFFFF);
            }else {
                hoveredSlot -= 9;
                int slotX = leftPos + playerInvStartWidth + hoveredSlot % 9 * itemSlotSize;
                int slotY = topPos + playerInvStartHeight + hoveredSlot / 9 * itemSlotSize;

                context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80FFFFFF);
            }
        }

        // Draw player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9; // +9 to skip hotbar
                int slotX = leftPos + playerInvStartWidth + col * itemSlotSize;
                int slotY = topPos + playerInvStartHeight + row * itemSlotSize;

                // Draw the slot content
                ItemStack stack = player.getInventory().getItem(slotIndex);
                if (!stack.isEmpty()) {
                    context.renderItem(stack, slotX, slotY);
                    context.renderItemDecorations(this.font, stack, slotX, slotY);
                }
            }
        }

        // Draw player hotbar slots
        for (int col = 0; col < 9; col++) {
            int slotX = leftPos + 8 + col * itemSlotSize;
            int slotY = topPos + playerHotbarStartHeight;

            // Draw the slot content
            ItemStack stack = player.getInventory().getItem(col);
            if (!stack.isEmpty()) {
                context.renderItem(stack, slotX, slotY);
                context.renderItemDecorations(this.font, stack, slotX, slotY);
            }
        }

        // Draw dragged item
        if (!draggedStack.isEmpty()) {
            context.renderItem(draggedStack, mouseX - 8, mouseY - 8, 0, 100);
        }

        // Draw tooltip
        if (draggedStack.isEmpty()) {
            // Find slot under mouse
            int slotIndex = getPlayerSlotAt(mouseX, mouseY);
            if (slotIndex >= 0) {
                ItemStack stack;
                stack = player.getInventory().getItem(slotIndex);

                if (!stack.isEmpty()) {
                    context.renderComponentTooltip(this.font, getTooltipFromItem(this.minecraft, stack), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            palette.getWeights().sort((w1, w2) -> Integer.compare(w2.getWeight(), w1.getWeight()));
            palette.applyWeightInputField();
            minecraft.setScreen(new PaletteListScreen(minecraft));
            SaveHelper.saveSettings();
            return true;
        }

        // Handle Input
        if(this.selectedInput > -1) {  // Handle Weight Input
            WeightCategory weightCat = palette.getWeights().get(selectedInput);

            // Handle special keys
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !weightCat.getWeightInputField().isEmpty()) {

                if (!markedEntireInput)// Remove the last character
                    weightCat.setWeightInputField(weightCat.getWeightInputField().substring(0, weightCat.getWeightInputField().length() - 1));
                else {
                    weightCat.setWeightInputField("");
                    markedEntireInput = false;
                }
                return true;
            } else if (modifiers == 2 && keyCode == GLFW.GLFW_KEY_A) {
                markedEntireInput = true;
                return true;
            }
        }else if(selectedInput == -1){  // Handle title input
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !palette.getName().isEmpty()) {

                if (!markedEntireInput)// Remove the last character
                    palette.setName(palette.getName().substring(0, palette.getName().length() - 1));
                else {
                    palette.setName("");
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
    public boolean keyReleased(int i, int j, int k) {
        return super.keyReleased(i, j, k);
    }

    @Override
    public boolean charTyped(char ch, int modifiers) {

        if (this.selectedInput >= -1) {

            if(this.selectedInput >= 0) {  // Weight selected
                if(Character.isDigit(ch)) {
                    WeightCategory weightCat = palette.getWeights().get(selectedInput);

                    if (this.markedEntireInput) {
                        weightCat.setWeightInputField(ch + "");
                        this.markedEntireInput = false;
                        return true;
                    } else {

                        if (weightCat.getWeightInputField().length() == MAX_WEIGHT_LENGTH)
                            return false;

                        weightCat.setWeightInputField(weightCat.getWeightInputField() + ch);
                        return true;
                    }
                }
            }else{  // Title selected
                if (this.markedEntireInput) {
                    palette.setName(ch + "");
                    this.markedEntireInput = false;
                    return true;
                } else {

                    if (palette.getName().length() == MAX_TITLE_LENGTH)
                        return false;

                    palette.setName(palette.getName() + ch);
                    return true;
                }
            }
        }

        return super.charTyped(ch, modifiers);
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
    }

    @Override
    public boolean isFocused() {
        return super.isFocused();
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        return super.getCurrentFocusPath();
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return super.nextFocusPath(focusNavigationEvent);
    }

    public boolean isKeyDown(int keyCode) {
        long window = minecraft.getWindow().getWindow();
        int state = GLFW.glfwGetKey(window, keyCode);
        return state == GLFW.GLFW_PRESS || state == GLFW.GLFW_REPEAT;
    }

    private WeightRowInfo getRowsWeightInfo(int row, List<WeightCategory> weightCats){
        int rowCounter = -1;
        for (int weightCatNo = 0; weightCatNo < weightCats.size(); weightCatNo++) {
            rowCounter++; // Header
            WeightCategory weightCat = weightCats.get(weightCatNo);

            if(rowCounter == row)
                return new WeightRowInfo(weightCat.getWeight(), weightCatNo);

            for (int itemRowNo = 0; itemRowNo <= weightCat.getItems().size(); itemRowNo += 9) {
                rowCounter++;

                if(rowCounter == row){
                    List<ItemStack> rowItems = new ArrayList<>();
                    for (int itemNo = itemRowNo; itemNo < itemRowNo + 9; itemNo++) {
                        if(weightCat.getItems().size() == itemNo)
                            break;

                        rowItems.add(weightCat.getItems().get(itemNo));
                    }

                    return new WeightRowInfo(rowItems, weightCatNo);

                }
            }
        }

        return new WeightRowInfo();
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double d, double e) {
        return super.getChildAt(d, e);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int slotIndex = getPlayerSlotAt((int)mouseX, (int)mouseY);
        boolean handled = false;

        if (button == 1) { // Right click
            this.draggedStack = ItemStack.EMPTY;
            handled = true;
        }
        else if (button == 0) { // Left click
            this.markedEntireInput = false;

            if(slotIndex == -1) {
                List<WeightCategory> weights = palette.getWeights();
                final int scrollLevel = (int) scrollPosition;
                int xPos = leftPos + weightContainerStartWidth;

                // Set Icon slot
                if(!draggedStack.isEmpty() && isPointInRegion(leftPos + iconMarginX, topPos + iconMarginY, 16, 16, (int)mouseX, (int)mouseY)){
                    this.palette.setIcon(draggedStack.copy());
                    this.draggedStack = ItemStack.EMPTY;
                    handled = true;
                }

                // Click title
                if(isPointInRegion(leftPos + titleInputMarginX, topPos + titleInputMarginY, titleInputWidth, titleInputHeight, (int)mouseX, (int)mouseY)) {
                    this.selectedInput = -1;
                    this.selectedInputBlink = 0;
                    handled = true;
                }

                // Click export
                if(isPointInRegion(leftPos + exportButtonMarginX, topPos + exportButtonMarginY, ButtonCatalogue.smallButtonSize, ButtonCatalogue.smallButtonSize, (int)mouseX, (int)mouseY)) {
                    palette.exportToClipboard();
                    playButtonClickSound();
                    handled = true;
                }

                // Click on scroller
                if(isPointInRegion(leftPos + scrollMarginX, getCurrentScrollerYPosition(), scrollerWidth, scrollerHeight, (int)mouseX, (int)mouseY)) {
                    clickedOnScroller = true;
                    handled = true;
                }

                boolean addedAddBtn = false;
                for (int weightSlot = scrollLevel; weightSlot < scrollLevel + 8; weightSlot++) {
                    if(handled)
                        break;

                    WeightRowInfo rowInfo = getRowsWeightInfo(weightSlot, weights);

                    int yPos = topPos + weightContainerStartHeight + (weightSlot - scrollLevel) * weightItemsHeight;

                    if (rowInfo.isBlank() && !addedAddBtn) {  // Add new element
                        if (isPointInRegion(xPos, yPos, weightItemsWidth, weightItemsHeight, (int) mouseX, (int) mouseY)) {
                            this.palette.addWeight(new WeightCategory(100, new ArrayList<>()));
                            handled = true;
                            playButtonClickSound();
                            break;
                        }
                        addedAddBtn = true;
                    } else if (rowInfo.isWeightRow()) {  // Clicked on weight header
                        // On delete click
                        if (isPointInRegion(xPos + deleteButtonMarginX, yPos + deleteButtonMarginY, buttonSize, buttonSize, (int) mouseX, (int) mouseY)) {
                            if(deleteConfirm < 1) {
                                this.deleteConfirm = DELETE_DOUBLE_CLICK_DURATION;
                                this.toBeDeletedId = rowInfo.weightCategoryId;
                            }
                            else if(this.toBeDeletedId == rowInfo.weightCategoryId){
                               this.removeWeight(rowInfo.weightCategoryId);
                            }
                            handled = true;
                            playButtonClickSound();
                            break;
                        }
                        // Click somewhere else on header
                        else if (isPointInRegion(xPos, yPos, weightItemsWidth, weightItemsHeight, (int) mouseX, (int) mouseY)) {
                            this.selectedInput = rowInfo.weightCategoryId;
                            handled = true;
                            this.selectedInputBlink = 0;
                            break;
                        }
                    }else if(!rowInfo.isBlank()){  // Clicked on inventory area
                        if (!draggedStack.isEmpty() && isPointInRegion(xPos, yPos, weightItemsWidth, weightItemsHeight, (int) mouseX, (int) mouseY)) {
                            weights.get(rowInfo.weightCategoryId).addItem(draggedStack.copy());
                            this.draggedStack = ItemStack.EMPTY;
                            handled = true;
                            break;
                        } else if (draggedStack.isEmpty() && isPointInRegion(xPos, yPos, weightItemsWidth, weightItemsHeight, (int) mouseX, (int) mouseY)) {
                            int clickedRowSlot = getPaletteSlotAt((int)mouseX, (int)mouseY);
                            if(rowInfo.items.size() > clickedRowSlot) {
                                ItemStack itemToRemove = rowInfo.items.get(clickedRowSlot);
                                weights.get(rowInfo.weightCategoryId).removeItem(itemToRemove);
                                handled = true;
                                break;
                            }
                        }
                    }
                }
            }else{  // Clicked on player inventory
                ItemStack playerStack = player.getInventory().getItem(slotIndex);
                this.draggedStack = playerStack.copy();
                this.draggedStack.setCount(1);
                handled = true;
            }
        }

        if(this.deleteConfirm < DELETE_DOUBLE_CLICK_DURATION)
            this.deleteConfirm = 0;

        if(!handled)
            draggedStack = ItemStack.EMPTY;

        return handled;
    }

    private void removeWeight(int i){
        if(this.selectedInput == i)
            this.selectedInput = -2;

        this.deleteConfirm = 0;

        this.palette.removeWeight(i);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        if (button == 0){
            if(clickedOnScroller){
                clickedOnScroller = false;
                return true;
            }

            if(this.draggedStack.isEmpty()){
                return false;
            }

            // Set icon
            if(isPointInRegion(leftPos + iconMarginX, topPos + iconMarginY, 16, 16, (int)mouseX, (int)mouseY)){
                this.palette.setIcon(draggedStack.copy());
                this.draggedStack = ItemStack.EMPTY;
                return true;
            }

            List<WeightCategory> weights = palette.getWeights();
            final int scrollLevel = (int) scrollPosition;
            int xPos = leftPos + weightContainerStartWidth;

            boolean addedAddBtn = false;
            for (int weightSlot = scrollLevel; weightSlot < scrollLevel + 8; weightSlot++) {

                WeightRowInfo rowInfo = getRowsWeightInfo(weightSlot, weights);

                int yPos = topPos + weightContainerStartHeight + (weightSlot - scrollLevel) * weightItemsHeight;

                if (rowInfo.isBlank() && !addedAddBtn) {  // Add new element
                    addedAddBtn = true;
                } else if (rowInfo.isWeightRow()) {  // Clicked on weight header
                    // On delete click
                    if (isPointInRegion(xPos, yPos, weightItemsWidth, weightItemsHeight, (int) mouseX, (int) mouseY)) {
                        weights.get(rowInfo.weightCategoryId).addItem(draggedStack.copy());
                        this.draggedStack = ItemStack.EMPTY;

                        return true;
                    }
                } else if (!rowInfo.isBlank()) {  // Clicked on inventory area
                    if (isPointInRegion(xPos, yPos, weightItemsWidth, weightItemsHeight, (int) mouseX, (int) mouseY)) {
                        weights.get(rowInfo.weightCategoryId).addItem(draggedStack.copy());
                        this.draggedStack = ItemStack.EMPTY;
                        return true;
                    }
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {

        if(clickedOnScroller) {
            double scrollStepHeight = scrollbarHeight / (double) palette.getScreenRowCount();
            double relativeMousePosY = mouseY - topPos - scrollMarginY + (scrollStepHeight / 2);

            if (relativeMousePosY <= 0) {
                scrollPosition = 0;
            } else if (relativeMousePosY >= scrollbarHeight) {
                scrollPosition = palette.getScreenRowCount();
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
        else if(scrollPosition >= palette.getScreenRowCount())
            scrollPosition = palette.getScreenRowCount();

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    /**
     * Get the slot index at the given mouse coordinates.
     * Returns -1 if no slot is at the position.
     */
    private int getPlayerSlotAt(int mouseX, int mouseY) {

        // Check player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9; // +9 to skip hotbar
                int slotX = leftPos + playerInvStartWidth + col * 18;
                int slotY = topPos + playerInvStartHeight + row * 18;

                if (isPointInRegion(slotX, slotY, 16, 16, mouseX, mouseY)) {
                    return slotIndex;
                }
            }
        }

        // Check player hotbar slots
        for (int col = 0; col < 9; col++) {
            int slotX = leftPos + 8 + col * 18;
            int slotY = topPos + playerHotbarStartHeight;

            if (isPointInRegion(slotX, slotY, 16, 16, mouseX, mouseY)) {
                return col;
            }
        }

        return -1;
    }

    @Override
    public void containerTick(){
        if(deleteConfirm > 0)
            deleteConfirm--;

        selectedInputBlink++;

        if(selectedInputBlink >= SELECTED_INPUT_BLINK_DURATION * 2)
            selectedInputBlink = 0;
    }

    /**
     * Get the slot index at the given mouse coordinates. Assumes that the mouse is within row's bounds!
     */
    private int getPaletteSlotAt(int mouseX, int mouseY) {

        int relativeMouseX = mouseX - weightContainerStartWidth - leftPos;
        return relativeMouseX / itemSlotSize;
    }

    private int getCurrentScrollerYPosition(){
        final int scrollLevel = (int)scrollPosition;

        if(palette.getScreenRowCount() == 0){
            return topPos + scrollMarginY;
        }

        return (int)(topPos + scrollMarginY + ((float)scrollLevel / palette.getScreenRowCount() * (scrollbarHeight - scrollerHeight)));
    }

    private boolean isPointInRegion(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }

    @Override
    public int getTabOrderGroup() {
        return super.getTabOrderGroup();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public void setDraggedStack(ItemStack stack){
        this.draggedStack = stack;
    }

    public ItemStack getDraggedStack(){
        return this.draggedStack;
    }

    public static void playButtonClickSound() {
        Minecraft client = Minecraft.getInstance();
        client.getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
        );
    }

    private class WeightRowInfo {
        public int weight;
        public List<ItemStack> items;
        public int weightCategoryId;

        public WeightRowInfo(int weight, List<ItemStack> items, int weightCategoryId){
            this.weight = weight;
            this.weightCategoryId = weightCategoryId;
            this.items = items;
        }

        public WeightRowInfo(int weight, int weightCategoryId){
            this.weight = weight;
            this.weightCategoryId = weightCategoryId;
            this.items = null;
        }

        public WeightRowInfo(List<ItemStack> items, int weightCategoryId){
            this.weight = -1;
            this.weightCategoryId = weightCategoryId;
            this.items = items;
        }

        public WeightRowInfo(){
            this.weight = -1;
            this.weightCategoryId = -1;
            this.items = null;
        }

        public boolean isWeightRow(){
            return weight > -1;
        }

        public boolean isBlank(){
            return weightCategoryId == -1;
        }
    }
}
