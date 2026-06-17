package com.norako.fracturia;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public class FracturiaAttachments {
    public static final AttachmentType<Integer> VOIDED_LEVEL = AttachmentRegistry.createDefaulted(
            Identifier.of(Fracturia.MOD_ID, "voided_level"),
            () -> 0
    );

    public static void register() {}
}