package com.mocretion.blockpalettes;

import com.mocretion.blockpalettes.data.Palette;
import com.mocretion.blockpalettes.data.PaletteManager;
import com.mocretion.blockpalettes.data.WeightCategory;
import com.mocretion.blockpalettes.gui.hud.HudRenderer;
import com.mocretion.blockpalettes.gui.screens.PaletteEditScreen;
import com.mocretion.blockpalettes.gui.screens.PaletteListScreen;
import com.mocretion.blockpalettes.gui.screens.menutypes.AllMenus;
import com.mocretion.blockpalettes.gui.screens.menutypes.EditMenu;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BlockPalettesClient implements ClientModInitializer {
	public static final String MOD_ID = "blockpalettes";
	public static final String MOD_NAME = "BlockPalettes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyMapping listPalettesKey;

	private static final List<PendingBlockCheck> pendingChecks = new ArrayList<>();

	public static final RandomSource random = RandomSource.create();

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// populizePalettesTest();

		MenuScreens.register(AllMenus.EDIT_MENU_HANDLER, (MenuScreens.ScreenConstructor<EditMenu, PaletteEditScreen>)PaletteEditScreen::new);

		registerKeyBidnings();
	}

	private void registerKeyBidnings(){

		if (listPalettesKey == null) {
			listPalettesKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
					"key.blockpalettes.open_palettes_screen",
					InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_B, // B to open palettes
					"category.blockpalettes.keys"
			));
		}

		// This will be executed on the client initialization
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Check if the key was pressed this tick
			while (listPalettesKey.consumeClick()) {

				PaletteManager.initBySaveFile();
				openPaletteListScreen(client);
			}
		});

		// Add about-to-be-placed-blocks to a list
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->{
			if(world.isClientSide() && player instanceof LocalPlayer){

				BlockPos blockPos = hitResult.getBlockPos();
				BlockState state = world.getBlockState(blockPos);

				if(state.canBeReplaced()){  // Block was placed in snow, grass, fern, etc
					pendingChecks.add(new PendingBlockCheck(world, player, blockPos, state));
				}else{  // Block was placed next to block the player was looking at
					BlockPos blockPosSideBlock = hitResult.getBlockPos().relative(hitResult.getDirection());
					BlockState stateSideBlock = world.getBlockState(blockPosSideBlock);

					pendingChecks.add(new PendingBlockCheck(world, player, blockPosSideBlock, stateSideBlock));
				}
			}
            return InteractionResult.PASS;
        });

		// Add check if blocks were actually placed next tick
		ClientTickEvents.END_CLIENT_TICK.register(server -> {
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

		});

		HudRenderCallback.EVENT.register(new HudRenderer()::renderHudAdditions);
	}

	private void onBlockPlaced(Player player) {

		if(!PaletteManager.getIsEnabled())
			return;

		if(!PaletteManager.getSelectedPalettes().containsKey(player.getInventory().selected + 1))
			return;

		PaletteManager.getSelectedPalettes().get(player.getInventory().selected + 1).getPaletteItemFromInventory(player);
	}

	private void openPaletteListScreen(Minecraft client) {
		client.setScreen(new PaletteListScreen(client));
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

