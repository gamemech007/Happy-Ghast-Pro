package com.anantaya.happyghastpro.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

public final class HappyGhastRecallHandler {

    private static final double SEARCH_RADIUS = 128.0D;
    private static final double TELEPORT_DISTANCE_IN_FRONT = 5.0D;
    private static final double TELEPORT_HEIGHT_ABOVE_PLAYER = 3.0D;

    private HappyGhastRecallHandler() {
    }

    public static void recallHappyGhast(Player player) {
        System.out.println("[HAPPY GHAST PRO] recallHappyGhast called by: " + player.getName().getString());

        if (!(player instanceof ServerPlayer serverPlayer)) {
            System.out.println("[HAPPY GHAST PRO] Player is not ServerPlayer");
            return;
        }

        if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) {
            System.out.println("[HAPPY GHAST PRO] Level is not ServerLevel");
            return;
        }

        HappyGhast happyGhast = findNearestHappyGhast(serverLevel, serverPlayer);

        if (happyGhast == null) {
            System.out.println("[HAPPY GHAST PRO] No Happy Ghast found within search radius");

            serverPlayer.sendSystemMessage(
                    Component.literal("No Happy Ghast heard your call.")
                            .withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        System.out.println("[HAPPY GHAST PRO] Found Happy Ghast: " + happyGhast.getUUID());

        double targetX = serverPlayer.getX() + serverPlayer.getLookAngle().x * TELEPORT_DISTANCE_IN_FRONT;
        double targetY = serverPlayer.getY() + TELEPORT_HEIGHT_ABOVE_PLAYER;
        double targetZ = serverPlayer.getZ() + serverPlayer.getLookAngle().z * TELEPORT_DISTANCE_IN_FRONT;

        System.out.println("[HAPPY GHAST PRO] Teleporting to: " + targetX + ", " + targetY + ", " + targetZ);

        happyGhast.teleportTo(targetX, targetY, targetZ);

        serverPlayer.sendSystemMessage(
                Component.literal("Your Happy Ghast answered the horn!")
                        .withStyle(ChatFormatting.AQUA)
        );
    }

    private static HappyGhast findNearestHappyGhast(ServerLevel level, ServerPlayer player) {
        AABB searchBox = player.getBoundingBox().inflate(SEARCH_RADIUS);

        List<HappyGhast> ghasts = level.getEntitiesOfClass(
                HappyGhast.class,
                searchBox,
                happyGhast -> happyGhast.isAlive() && !happyGhast.isRemoved()
        );

        return ghasts.stream()
                .filter(happyGhast -> {
                    if (!(happyGhast instanceof com.anantaya.happyghastpro.access.HappyGhastBindingAccess access)) {
                        return false;
                    }

                    return access.happyGhastPro$isBoundTo(player.getUUID());
                })
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }
}