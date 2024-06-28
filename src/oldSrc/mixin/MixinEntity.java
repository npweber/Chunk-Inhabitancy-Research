package net.npwdev.npweber.rng.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.npwdev.npweber.rng.RNG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow public abstract EntityType<?> getType();

    @Inject(method = "setYRot", at = @At("HEAD"))
    public void setYRot(float yRot, CallbackInfo info) {
        if(this.getType().equals(EntityType.ITEM) && RNG.shouldCaptureItem()) {
            RNG.disableItemCapture();
            RNG.calculateItemYRotSeedLeftBits(yRot);
        }

    }
}
