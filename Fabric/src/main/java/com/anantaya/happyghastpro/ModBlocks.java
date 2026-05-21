package com.anantaya.happyghastpro;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

    public static final Identifier GHAST_ANCHOR_ID =
            Identifier.fromNamespaceAndPath(HappyGhastPro.MOD_ID, "ghast_anchor");

    public static final ResourceKey<Block> GHAST_ANCHOR_BLOCK_KEY =
            ResourceKey.create(Registries.BLOCK, GHAST_ANCHOR_ID);

    public static final ResourceKey<Item> GHAST_ANCHOR_ITEM_KEY =
            ResourceKey.create(Registries.ITEM, GHAST_ANCHOR_ID);

    public static final Block GHAST_ANCHOR = Registry.register(
            BuiltInRegistries.BLOCK,
            GHAST_ANCHOR_ID,
            new GhastAnchorBlock(
                    BlockBehaviour.Properties.of()
                            .setId(GHAST_ANCHOR_BLOCK_KEY)
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(4.0f, 1200.0f)
                            .sound(SoundType.STONE)
                            .noOcclusion()
                            .lightLevel(state -> 10)
            )
    );

    public static final Item GHAST_ANCHOR_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            GHAST_ANCHOR_ID,
            new BlockItem(
                    GHAST_ANCHOR,
                    new Item.Properties()
                            .setId(GHAST_ANCHOR_ITEM_KEY)
            )
    );

    public static void registerModBlocks() {
        HappyGhastPro.LOGGER.info("Registering Ghast Anchor for " + HappyGhastPro.MOD_ID);
    }
}