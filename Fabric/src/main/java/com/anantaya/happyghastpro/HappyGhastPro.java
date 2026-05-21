package com.anantaya.happyghastpro;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HappyGhastPro implements ModInitializer {
	public static final String MOD_ID = "happy-ghast-pro";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
				.register(output -> output.accept(ModBlocks.GHAST_ANCHOR_ITEM));

		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (level.isClientSide()) {
				return InteractionResult.PASS;
			}

			if (!player.isShiftKeyDown() && !player.isSecondaryUseActive()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hitResult.getBlockPos();

			if (!level.getBlockState(pos).is(ModBlocks.GHAST_ANCHOR)) {
				return InteractionResult.PASS;
			}

			if (level instanceof ServerLevel serverLevel) {
				return GhastAnchorBlock.unbindFromAnchor(serverLevel, pos, player);
			}

			return InteractionResult.PASS;
		});

		LOGGER.info("Happy Ghast Pro loaded");
	}
}