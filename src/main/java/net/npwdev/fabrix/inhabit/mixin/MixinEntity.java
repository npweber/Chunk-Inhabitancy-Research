package net.npwdev.fabrix.inhabit.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.npwdev.fabrix.inhabit.Inhabitancy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow public abstract EntityType<?> getType();

    @Inject(method = "setYRot", at = @At("HEAD"))
    public void setYRot(float p_146923_, CallbackInfo info) {
        if (getType().equals(EntityType.ITEM)) {
            Inhabitancy.determineLeftSeedBits(p_146923_);
        }
    }

}
