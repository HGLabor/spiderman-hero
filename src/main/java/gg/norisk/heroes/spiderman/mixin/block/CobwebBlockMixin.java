package gg.norisk.heroes.spiderman.mixin.block;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import gg.norisk.heroes.spiderman.player.SpidermanPlayerKt;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CobwebBlock.class)
public abstract class CobwebBlockMixin {
    @WrapWithCondition(
            method = "onEntityCollision",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;slowMovement(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Vec3d;)V")
    )
    private boolean onlySlowIfAllowed(Entity instance, BlockState blockState, Vec3d vec3d) {
        if (instance instanceof PlayerEntity player) {
            return !SpidermanPlayerKt.isSpiderman(player);
        } else {
            return true;
        }
    }
}
