package com.mocretion.blockpalettes;

import net.minecraft.client.KeyMapping;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import com.mocretion.blockpalettes.data.Palette;
import com.mocretion.blockpalettes.data.PaletteManager;
import com.mocretion.blockpalettes.data.WeightCategory;
import com.mocretion.blockpalettes.gui.hud.HudRenderer;
import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;
import com.mocretion.blockpalettes.gui.screens.PaletteListScreen;
import com.mocretion.blockpalettes.gui.screens.menutypes.AllMenus;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BlockPalettes.MOD_ID)
public class BlockPalettes
{
    public static final String MOD_ID = "blockpalettes";
    public static final String MOD_NAME = "BlockPalettes";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static KeyMapping listPalettesKey;

    private static final List<PendingBlockCheck> pendingChecks = new ArrayList<>();

    public static final RandomSource random = RandomSource.create();
    private static final ResourceLocation SELECTED_SLOT_HOTBAR = ResourceLocation.fromNamespaceAndPath(MOD_ID, "selected-slots-hotbar");


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public BlockPalettes(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::registerKeyBindings);
        modEventBus.addListener(this::registerOverlays);

        AllMenus.register(modEventBus);
    }

    private void registerKeyBindings(final RegisterKeyMappingsEvent event) {
        // Create the key binding
        listPalettesKey = new KeyMapping(
                "key.blockpalettes.open_palettes_screen",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.blockpalettes.keys"
        );

        // Register the key binding
        event.register(listPalettesKey);
    }

    private void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, SELECTED_SLOT_HOTBAR, new HudRenderer()::render);
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientGameEvents
    {
        @SubscribeEvent
        public static void onClientBlockPlace(PlayerInteractEvent.RightClickBlock event){

            Level world = event.getLevel();
            if(event.getLevel().isClientSide() && event.getEntity() instanceof LocalPlayer){

                BlockPos blockPos = event.getPos();
                BlockState state = world.getBlockState(blockPos);

                if(state.canBeReplaced()){  // Block was placed in snow, grass, fern, etc
                    pendingChecks.add(new PendingBlockCheck(world, event.getEntity(), blockPos, state));
                }else{  // Block was placed next to block the player was looking at
                    Direction face = event.getFace();

                    if(face == null)
                        return;

                    BlockPos blockPosSideBlock = event.getPos().relative(event.getFace());
                    BlockState stateSideBlock = world.getBlockState(blockPosSideBlock);

                    pendingChecks.add(new PendingBlockCheck(world, event.getEntity(), blockPosSideBlock, stateSideBlock));
                }

            }
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event){
            if (!pendingChecks.isEmpty()) {
                Iterator<PendingBlockCheck> iterator = pendingChecks.iterator();

                while (iterator.hasNext()) {
                    PendingBlockCheck check = iterator.next();
                    Level world = check.world;
                    if(world == null){
                        iterator.remove();
                        continue;
                    }

                    BlockState currentState = world.getBlockState(check.pos);

                    if (!currentState.equals(check.originalState)) {
                        onBlockPlaced(check.player);
                        break;
                    }

                    iterator.remove();
                }
            }

            pendingChecks.clear();
        }

        private static void onBlockPlaced(Player player) {

            if(!PaletteManager.getIsEnabled())
                return;

            if(!PaletteManager.getSelectedPalettes().containsKey(player.getInventory().selected + 1))
                return;

            PaletteManager.getSelectedPalettes().get(player.getInventory().selected + 1).getPaletteItemFromInventory(player);
        }


        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            // Check if our key is pressed
            if (listPalettesKey.consumeClick()) {
                // This is where you put the code you want to execute when the key is pressed
                PaletteManager.initBySaveFile();
                openPaletteListScreen(Minecraft.getInstance());
            }
        }

        private static void openPaletteListScreen(Minecraft client) {
            client.setScreen(new PaletteListScreen(client));
        }
    }

    private static class PendingBlockCheck{
        final Level world;
        final Player player;
        final BlockPos pos;
        final BlockState originalState;

        PendingBlockCheck(Level world, Player player, BlockPos pos, BlockState originalState) {
            this.world = world;
            this.player = player;
            this.pos = pos;
            this.originalState = originalState;
        }
    }

    private static void populizePalettesTest(){
        List<WeightCategory> wcList = new ArrayList<>();
        WeightCategory wc1 = new WeightCategory(10, new ArrayList<>());
        wc1.addItem(new ItemStack(Items.MOSS_BLOCK));
        wc1.addItem(new ItemStack(Items.DIRT));

        WeightCategory wc2 = new WeightCategory(5, new ArrayList<>());
        wc2.addItem(new ItemStack(Items.GRASS_BLOCK));

        wcList.add(wc1);
        wcList.add(wc2);

        PaletteManager.getBuilderPalettes().add(new Palette("My Test Palette", new ItemStack(Items.GRASS_BLOCK), 9, wcList));


        List<WeightCategory> wcList2 = new ArrayList<>();
        WeightCategory wc3 = new WeightCategory(10, new ArrayList<>());
        wc3.addItem(new ItemStack(Items.DIORITE));
        wc3.addItem(new ItemStack(Items.STONE));

        WeightCategory wc4 = new WeightCategory(5, new ArrayList<>());
        wc4.addItem(new ItemStack(Items.GRANITE));
        wc4.addItem(new ItemStack(Items.GRANITE_SLAB));
        wc4.addItem(new ItemStack(Items.CHISELED_STONE_BRICKS));

        wcList2.add(wc3);
        wcList2.add(wc4);

        PaletteManager.getBuilderPalettes().add(new Palette("Other Palette", new ItemStack(Items.STONE), 8, wcList2));
    }
}
