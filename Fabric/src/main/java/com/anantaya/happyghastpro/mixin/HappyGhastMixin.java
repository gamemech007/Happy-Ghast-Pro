package com.anantaya.happyghastpro.mixin;

import com.anantaya.happyghastpro.access.HappyGhastBindingAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.anantaya.happyghastpro.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Optional;
import java.util.UUID;

@Mixin(HappyGhast.class)
public class HappyGhastMixin implements HappyGhastBindingAccess {

    @Inject(
            method = "customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At("TAIL")
    )
    private void happyGhastPro$returnToAnchor(ServerLevel level, CallbackInfo ci) {
        HappyGhast self = (HappyGhast) (Object) this;

        if (self.isBaby()) {
            return;
        }

        if (!this.happyGhastPro$hasBoundAnchor()) {
            return;
        }

        BlockPos anchorPos = this.happyGhastPro$getBoundAnchor();

        if (!level.getBlockState(anchorPos).is(ModBlocks.GHAST_ANCHOR)) {
            this.happyGhastPro$clearBinding();
            return;
        }

        if (self.isVehicle() || self.isLeashed()) {
            return;
        }

        if (this.happyGhastPro$returnToAnchorDelayTicks > 0) {
            this.happyGhastPro$returnToAnchorDelayTicks--;
            return;
        }

        Vec3 anchorCenter = anchorPos.getCenter().add(0.0D, 5.0D, 0.0D);
        double distanceSqr = self.distanceToSqr(anchorCenter);

        if (distanceSqr < 18.0D * 18.0D) {
            return;
        }

        boolean playerCanStillSeeOrLoadGhast = level.players().stream()
                .anyMatch(player ->
                        !player.isSpectator()
                                && player.distanceToSqr(self) < 96.0D * 96.0D
                );

        /*
         * If very far from anchor but still near/visible to a player,
         * do NOT teleport. Let it glide back naturally.
         */
        if (distanceSqr > 160.0D * 160.0D && !playerCanStillSeeOrLoadGhast) {
            self.teleportTo(
                    anchorCenter.x,
                    anchorCenter.y,
                    anchorCenter.z
            );

            self.setDeltaMovement(Vec3.ZERO);

            self.getMoveControl().setWantedPosition(
                    anchorCenter.x,
                    anchorCenter.y,
                    anchorCenter.z,
                    0.6D
            );

            return;
        }

        double speed = distanceSqr > 48.0D * 48.0D ? 1.2D : 0.8D;

        self.getMoveControl().setWantedPosition(
                anchorCenter.x,
                anchorCenter.y,
                anchorCenter.z,
                speed
        );
    }

    @Inject(
            method = "removePassenger(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("TAIL")
    )
    private void happyGhastPro$delayReturnAfterDismount(Entity passenger, CallbackInfo ci) {
        if (passenger instanceof Player) {
            this.happyGhastPro$returnToAnchorDelayTicks = 60;
        }
    }

    @Unique
    private static final String HAPPY_GHAST_PRO_BOUND_OWNER_KEY = "happy_ghast_pro_bound_owner";

    @Unique
    private static final String HAPPY_GHAST_PRO_BOUND_ANCHOR_KEY = "happy_ghast_pro_bound_anchor";

    @Unique
    private int happyGhastPro$returnToAnchorDelayTicks;

    @Unique
    private UUID happyGhastPro$boundOwner;

    @Unique
    private BlockPos happyGhastPro$boundAnchor;

    @Unique
    private long happyGhastPro$snowBoostEndTick = -1L;

    @Unique
    private boolean happyGhastPro$snowBoostFadeMessageShown = true;

    @Override
    public void happyGhastPro$clearBinding() {
        this.happyGhastPro$boundOwner = null;
        this.happyGhastPro$boundAnchor = null;
    }

    @Override
    public void happyGhastPro$setBoundOwner(UUID ownerUuid) {
        this.happyGhastPro$boundOwner = ownerUuid;
    }

    @Override
    public UUID happyGhastPro$getBoundOwner() {
        return this.happyGhastPro$boundOwner;
    }

    @Override
    public boolean happyGhastPro$hasBoundOwner() {
        return this.happyGhastPro$boundOwner != null;
    }

    @Override
    public boolean happyGhastPro$isBoundTo(UUID ownerUuid) {
        return this.happyGhastPro$boundOwner != null
                && this.happyGhastPro$boundOwner.equals(ownerUuid);
    }

    @Override
    public void happyGhastPro$setBoundAnchor(BlockPos anchorPos) {
        this.happyGhastPro$boundAnchor = anchorPos;
    }

    @Override
    public BlockPos happyGhastPro$getBoundAnchor() {
        return this.happyGhastPro$boundAnchor;
    }

    @Override
    public boolean happyGhastPro$hasBoundAnchor() {
        return this.happyGhastPro$boundAnchor != null;
    }

    @Inject(
            method = "tick()V",
            at = @At("TAIL")
    )
    private void happyGhastPro$tickSnowBoost(CallbackInfo ci) {
        HappyGhast self = (HappyGhast) (Object) this;

        if (self.level().isClientSide()) {
            return;
        }

        if (this.happyGhastPro$snowBoostEndTick < 0L) {
            return;
        }

        if (self.level().getGameTime() <= this.happyGhastPro$snowBoostEndTick) {
            return;
        }

        if (!this.happyGhastPro$snowBoostFadeMessageShown) {
            this.happyGhastPro$snowBoostFadeMessageShown = true;

            if (self.getControllingPassenger() instanceof Player player) {
                player.sendSystemMessage(
                        Component.literal("❄ Snow Boost faded.")
                                .withStyle(ChatFormatting.GRAY)
                );
            }
        }

        this.happyGhastPro$snowBoostEndTick = -1L;
    }

    @Inject(
            method = "getRiddenInput(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void happyGhastPro$snowBoostRiddenSpeed(
            Player controller,
            Vec3 selfInput,
            CallbackInfoReturnable<Vec3> cir
    ) {
        HappyGhast self = (HappyGhast) (Object) this;

        if (this.happyGhastPro$snowBoostEndTick < 0L
                || self.level().getGameTime() > this.happyGhastPro$snowBoostEndTick) {
            return;
        }

        float strafe = controller.xxa;
        float forward = 0.0F;
        float up = 0.0F;

        if (controller.zza != 0.0F) {
            float pitchRadians = controller.getXRot() * ((float) Math.PI / 180.0F);

            float forwardLook = (float) Math.cos(pitchRadians);
            float upLook = -((float) Math.sin(pitchRadians));

            if (controller.zza < 0.0F) {
                forwardLook *= -0.5F;
                upLook *= -0.5F;
            }

            up = upLook;
            forward = forwardLook;
        }

        if (controller.isJumping()) {
            up += 0.5F;
        }

        double vanillaSpeed = self.getAttributeValue(Attributes.FLYING_SPEED);

        /*
         * Vanilla multiplier is 3.9.
         * Boosted snow speed uses 7.0 for a strong but not insane travel boost.
         */
        double boostedMultiplier = 8.0D;

        cir.setReturnValue(
                new Vec3(
                        (double) strafe,
                        (double) up,
                        (double) forward
                ).scale(boostedMultiplier * vanillaSpeed)
        );
    }

    @Inject(
            method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void happyGhastPro$bindWithGoatHorn(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        ItemStack stack = player.getItemInHand(hand);

        HappyGhast self = (HappyGhast) (Object) this;

        if (stack.is(Items.SNOWBALL)) {
            if (self.isBaby()) {
                return;
            }

            // Set on BOTH client and server so ridden movement actually feels boosted.
            this.happyGhastPro$snowBoostEndTick = self.level().getGameTime() + 20 * 30;
            this.happyGhastPro$snowBoostFadeMessageShown = false;

            if (!self.level().isClientSide()) {
                stack.consume(1, player);

                player.sendSystemMessage(
                        Component.literal("❄ Snow Boost activated: 30s")
                                .withStyle(ChatFormatting.AQUA)
                );
            }

            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        if (!stack.is(Items.GOAT_HORN)) {
            return;
        }

        if (self.isBaby()) {
            return;
        }

        if (!self.level().isClientSide()) {
            UUID playerUuid = player.getUUID();

            if (player.isSecondaryUseActive()) {
                if (!this.happyGhastPro$hasBoundOwner()) {
                    player.sendSystemMessage(
                            Component.literal("This Happy Ghast is not bound to anyone.")
                                    .withStyle(ChatFormatting.GRAY)
                    );
                } else if (!this.happyGhastPro$isBoundTo(playerUuid)) {
                    player.sendSystemMessage(
                            Component.literal("You cannot unbind another player's Happy Ghast.")
                                    .withStyle(ChatFormatting.RED)
                    );
                } else {
                    this.happyGhastPro$clearBinding();

                    player.sendSystemMessage(
                            Component.literal("Happy Ghast unbound from your goat horn.")
                                    .withStyle(ChatFormatting.YELLOW)
                    );
                }

                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            if (this.happyGhastPro$hasBoundOwner()
                    && !this.happyGhastPro$isBoundTo(playerUuid)) {
                player.sendSystemMessage(
                        Component.literal("This Happy Ghast is already bound to another player.")
                                .withStyle(ChatFormatting.RED)
                );
            } else {
                this.happyGhastPro$setBoundOwner(playerUuid);

                player.sendSystemMessage(
                        Component.literal("This Happy Ghast is now bound to your goat horn.")
                                .withStyle(ChatFormatting.AQUA)
                );
            }
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(
            method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V",
            at = @At("TAIL")
    )
    private void happyGhastPro$saveBoundData(ValueOutput tag, CallbackInfo ci) {
        if (this.happyGhastPro$boundOwner != null) {
            tag.putString(
                    HAPPY_GHAST_PRO_BOUND_OWNER_KEY,
                    this.happyGhastPro$boundOwner.toString()
            );
        }

        if (this.happyGhastPro$boundAnchor != null) {
            tag.putString(
                    HAPPY_GHAST_PRO_BOUND_ANCHOR_KEY,
                    this.happyGhastPro$boundAnchor.getX()
                            + ","
                            + this.happyGhastPro$boundAnchor.getY()
                            + ","
                            + this.happyGhastPro$boundAnchor.getZ()
            );
        }
    }

    @Inject(
            method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V",
            at = @At("TAIL")
    )
    private void happyGhastPro$readBoundData(ValueInput tag, CallbackInfo ci) {
        Optional<String> savedOwner = tag.getString(HAPPY_GHAST_PRO_BOUND_OWNER_KEY);

        if (savedOwner.isEmpty()) {
            this.happyGhastPro$boundOwner = null;
        } else {
            try {
                this.happyGhastPro$boundOwner = UUID.fromString(savedOwner.get());
            } catch (IllegalArgumentException exception) {
                this.happyGhastPro$boundOwner = null;
            }
        }

        Optional<String> savedAnchor = tag.getString(HAPPY_GHAST_PRO_BOUND_ANCHOR_KEY);

        if (savedAnchor.isEmpty()) {
            this.happyGhastPro$boundAnchor = null;
            return;
        }

        try {
            String[] parts = savedAnchor.get().split(",");

            if (parts.length != 3) {
                this.happyGhastPro$boundAnchor = null;
                return;
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            this.happyGhastPro$boundAnchor = new BlockPos(x, y, z);
        } catch (NumberFormatException exception) {
            this.happyGhastPro$boundAnchor = null;
        }
    }
}