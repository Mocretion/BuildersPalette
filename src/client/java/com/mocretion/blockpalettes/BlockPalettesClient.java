package com.mocretion.blockpalettes;

import com.mocretion.blockpalettes.data.Palette;
import com.mocretion.blockpalettes.data.PaletteManager;
import com.mocretion.blockpalettes.data.WeightCategory;
import com.mocretion.blockpalettes.gui.hud.HudRenderer;
import com.mocretion.blockpalettes.gui.screens.PaletteListScreen;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockPalettesClient implements ClientModInitializer {
	public static final String MOD_ID = "blockpalettes";
	public static final String MOD_NAME = "BlockPalettes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyBinding listPalettesKey;

	private static final List<PendingBlockCheck> pendingChecks = new ArrayList<>();

	public static final Random random = Random.create();

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// populizePalettesTest();

		registerKeyBidnings();
	}

	private void registerKeyBidnings(){

		if (listPalettesKey == null) {
			listPalettesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
					"key.blockpalettes.open_palettes_screen",
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_B, // B to open palettes
					"category.blockpalettes.keys"
			));
		}

		// This will be executed on the client initialization
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Check if the key was pressed this tick
			while (listPalettesKey.wasPressed()) {

				PaletteManager.initBySaveFile();
				openPaletteListScreen(client);
			}
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->{
			if(world.isClient() && player instanceof ClientPlayerEntity){
				BlockPos blockPos = hitResult.getBlockPos().offset(hitResult.getSide());

				BlockState state = world.getBlockState(blockPos);

				pendingChecks.add(new PendingBlockCheck(world, player, blockPos, state));

			}
            return ActionResult.PASS;
        });

		ClientTickEvents.END_CLIENT_TICK.register(server -> {
			if (!pendingChecks.isEmpty()) {
				Iterator<PendingBlockCheck> iterator = pendingChecks.iterator();

				while (iterator.hasNext()) {
					PendingBlockCheck check = iterator.next();
					World world = check.world;
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

	private void onBlockPlaced(PlayerEntity player) {

		if(!PaletteManager.getIsEnabled())
			return;

		if(!PaletteManager.getSelectedPalettes().containsKey(player.getInventory().selectedSlot + 1))
			return;

		PaletteManager.getSelectedPalettes().get(player.getInventory().selectedSlot + 1).getPaletteItemFromInventory(player);
	}

	private void openPaletteListScreen(MinecraftClient client) {
		client.setScreen(new PaletteListScreen(client));
	}

	private static class PendingBlockCheck{
		final World world;
		final PlayerEntity player;
		final BlockPos pos;
		final BlockState originalState;

		PendingBlockCheck(World world, PlayerEntity player, BlockPos pos, BlockState originalState) {
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

