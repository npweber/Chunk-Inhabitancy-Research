package net.npwdev.npweber.rng.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.npwdev.npweber.rng.RNG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private static void popResource(Level p_152441_, Supplier<ItemEntity> p_152442_, ItemStack p_152443_, CallbackInfo info) {
        if(RNG.shouldCaptureItem()) {
            RNG.disableItemCapture();;
        }
    }
}
