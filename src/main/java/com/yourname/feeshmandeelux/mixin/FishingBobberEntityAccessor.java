package com.yourname.feeshmandeelux.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for FishingBobberEntity.state (private field).
 * Returns ordinal: FLYING=0, HOOKED_IN_ENTITY=1, BOBBING=2.
 */
@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityAccessor {

    @Accessor("state")
    Enum<?> feeshman$getState();
}
