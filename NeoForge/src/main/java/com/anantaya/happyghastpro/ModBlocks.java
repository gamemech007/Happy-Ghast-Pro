package com.anantaya.happyghastpro;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final Identifier GHAST_ANCHOR_ID =
            Identifier.fromNamespaceAndPath(HappyGhastPro.MOD_ID, "ghast_anchor");

    public static final ResourceKey<net.minecraft.world.level.block.Block> GHAST_ANCHOR_BLOCK_KEY =
            ResourceKey.create(Registries.BLOCK, GHAST_ANCHOR_ID);

    public static final ResourceKey<Item> GHAST_ANCHOR_ITEM_KEY =
            ResourceKey.create(Registries.ITEM, GHAST_ANCHOR_ID);

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(HappyGhastPro.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HappyGhastPro.MOD_ID);

    public static final DeferredBlock<GhastAnchorBlock> GHAST_ANCHOR =
            BLOCKS.register(
                    "ghast_anchor",
                    () -> new GhastAnchorBlock(
                            BlockBehaviour.Properties.of()
                                    .setId(GHAST_ANCHOR_BLOCK_KEY)
                                    .mapColor(MapColor.COLOR_PURPLE)
                                    .strength(4.0f, 1200.0f)
                                    .sound(SoundType.STONE)
                                    .noOcclusion()
                                    .lightLevel(state -> 10)
                    )
            );

    public static final DeferredItem<BlockItem> GHAST_ANCHOR_ITEM =
            ITEMS.register(
                    "ghast_anchor",
                    () -> new BlockItem(
                            GHAST_ANCHOR.get(),
                            new Item.Properties()
                                    .setId(GHAST_ANCHOR_ITEM_KEY)
                    )
            );

    public static void registerModBlocks(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);

        HappyGhastPro.LOGGER.info("Registering Ghast Anchor for " + HappyGhastPro.MOD_ID);
    }
}