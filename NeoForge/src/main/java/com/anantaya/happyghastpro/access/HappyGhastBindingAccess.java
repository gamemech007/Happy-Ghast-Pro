package com.anantaya.happyghastpro.access;

import net.minecraft.core.BlockPos;

import java.util.UUID;

public interface HappyGhastBindingAccess {

    void happyGhastPro$setBoundOwner(UUID ownerUuid);

    UUID happyGhastPro$getBoundOwner();

    boolean happyGhastPro$hasBoundOwner();

    boolean happyGhastPro$isBoundTo(UUID ownerUuid);

    void happyGhastPro$setBoundAnchor(BlockPos anchorPos);

    BlockPos happyGhastPro$getBoundAnchor();

    boolean happyGhastPro$hasBoundAnchor();

    void happyGhastPro$clearBinding();
}