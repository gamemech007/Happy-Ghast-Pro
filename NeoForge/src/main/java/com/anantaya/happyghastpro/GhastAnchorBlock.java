package com.anantaya.happyghastpro;

import com.anantaya.happyghastpro.access.HappyGhastBindingAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GhastAnchorBlock extends Block {

    private static final int SEARCH_RADIUS = 32;

    public GhastAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown() || player.isSecondaryUseActive()) {
            return GhastAnchorBlock.unbindFromAnchor(serverLevel, pos, player);
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }


    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown() || player.isSecondaryUseActive()) {
            return this.unbindFromAnchor(serverLevel, pos, player);
        }

        return this.bindNearestGhast(serverLevel, pos, player);
    }

    private InteractionResult bindNearestGhast(ServerLevel serverLevel, BlockPos pos, Player player) {
        UUID playerUuid = player.getUUID();

        AABB searchBox = new AABB(pos).inflate(SEARCH_RADIUS);

        List<HappyGhast> nearbyGhasts = serverLevel.getEntitiesOfClass(
                HappyGhast.class,
                searchBox,
                ghast -> ghast.isAlive() && !ghast.isBaby()
        );

        if (nearbyGhasts.isEmpty()) {
            player.sendSystemMessage(
                    Component.literal("No adult Happy Ghast found near this anchor.")
                            .withStyle(ChatFormatting.RED)
            );

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.BLOCKS,
                    1.0F,
                    0.6F
            );

            return InteractionResult.SUCCESS_SERVER;
        }

        HappyGhast nearestGhast = nearbyGhasts.stream()
                .min(Comparator.comparingDouble(ghast -> ghast.distanceToSqr(pos.getCenter())))
                .orElse(null);

        if (nearestGhast == null) {
            return InteractionResult.SUCCESS_SERVER;
        }

        HappyGhastBindingAccess bindingAccess = (HappyGhastBindingAccess) nearestGhast;

        if (bindingAccess.happyGhastPro$hasBoundOwner()
                && !bindingAccess.happyGhastPro$isBoundTo(playerUuid)
                && bindingAccess.happyGhastPro$hasBoundAnchor()
                && serverLevel.getBlockState(bindingAccess.happyGhastPro$getBoundAnchor()).is(ModBlocks.GHAST_ANCHOR)) {
            player.sendSystemMessage(
                    Component.literal("The nearest Happy Ghast is already bound to another player's active anchor.")
                            .withStyle(ChatFormatting.RED)
            );

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.BLOCKS,
                    1.0F,
                    0.5F
            );

            return InteractionResult.SUCCESS_SERVER;
        }

        bindingAccess.happyGhastPro$setBoundOwner(playerUuid);
        bindingAccess.happyGhastPro$setBoundAnchor(pos.immutable());

        player.sendSystemMessage(
                Component.literal("Happy Ghast bound to this Ghast Anchor.")
                        .withStyle(ChatFormatting.AQUA)
        );

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        return InteractionResult.SUCCESS_SERVER;
    }

    public static InteractionResult unbindFromAnchor(ServerLevel serverLevel, BlockPos pos, Player player) {
        AABB searchBox = new AABB(pos).inflate(256.0D);

        List<HappyGhast> boundGhasts = serverLevel.getEntitiesOfClass(
                HappyGhast.class,
                searchBox,
                ghast -> {
                    if (!ghast.isAlive()) {
                        return false;
                    }

                    HappyGhastBindingAccess access = (HappyGhastBindingAccess) ghast;

                    // Clear ghasts bound specifically to this anchor.
                    if (access.happyGhastPro$hasBoundAnchor()
                            && access.happyGhastPro$getBoundAnchor().equals(pos)) {
                        return true;
                    }

                    // Also clear stale/old bindings from earlier tests if the ghast is nearby.
                    return access.happyGhastPro$hasBoundOwner()
                            && ghast.distanceToSqr(pos.getCenter()) <= 64.0D * 64.0D;
                }
        );

        if (boundGhasts.isEmpty()) {
            player.sendSystemMessage(
                    Component.literal("No bound Happy Ghast found near this anchor.")
                            .withStyle(ChatFormatting.GRAY)
            );

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.BLOCKS,
                    1.0F,
                    0.7F
            );

            return InteractionResult.SUCCESS_SERVER;
        }

        for (HappyGhast ghast : boundGhasts) {
            HappyGhastBindingAccess access = (HappyGhastBindingAccess) ghast;
            access.happyGhastPro$clearBinding();
        }

        player.sendSystemMessage(
                Component.literal("Cleared binding from " + boundGhasts.size() + " Happy Ghast.")
                        .withStyle(ChatFormatting.YELLOW)
        );

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                SoundSource.BLOCKS,
                1.0F,
                0.8F
        );

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.7D;
        double y = pos.getY() + 0.8D + random.nextDouble() * 0.5D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.7D;

        double ySpeed = random.nextDouble() * 0.035D;

        level.addParticle(
                ParticleTypes.REVERSE_PORTAL,
                x,
                y,
                z,
                0.0D,
                ySpeed,
                0.0D
        );

        if (random.nextInt(120) == 0) {
            level.playLocalSound(
                    pos,
                    SoundEvents.RESPAWN_ANCHOR_AMBIENT,
                    SoundSource.BLOCKS,
                    0.45F,
                    0.8F + random.nextFloat() * 0.4F,
                    false
            );
        }
    }
}