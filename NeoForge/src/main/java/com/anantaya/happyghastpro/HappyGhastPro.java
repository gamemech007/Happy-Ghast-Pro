package com.anantaya.happyghastpro;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(HappyGhastPro.MOD_ID)
public class HappyGhastPro {
	public static final String MOD_ID = "happyghastpro";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public HappyGhastPro(IEventBus modEventBus) {
		ModBlocks.registerModBlocks(modEventBus);

		modEventBus.addListener(this::addCreativeTabItems);

		NeoForge.EVENT_BUS.register(this);

		LOGGER.info("Happy Ghast Pro loaded");
	}

	private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			event.accept(ModBlocks.GHAST_ANCHOR_ITEM.get());
		}
	}

	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getLevel().isClientSide()) {
			return;
		}

		if (!event.getEntity().isShiftKeyDown() && !event.getEntity().isSecondaryUseActive()) {
			return;
		}

		BlockPos pos = event.getPos();

		if (!event.getLevel().getBlockState(pos).is(ModBlocks.GHAST_ANCHOR.get())) {
			return;
		}

		if (event.getLevel() instanceof ServerLevel serverLevel) {
			InteractionResult result = GhastAnchorBlock.unbindFromAnchor(
					serverLevel,
					pos,
					event.getEntity()
			);

			if (result != InteractionResult.PASS) {
				event.setCanceled(true);
				event.setCancellationResult(result);
			}
		}
	}
}