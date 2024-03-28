package gg.norisk.heroes.spiderman.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import gg.norisk.heroes.spiderman.player.SpidermanPlayerKt;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "isClimbing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z")
    )
    private boolean onlyClimbIfAllowed(boolean original) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity player && SpidermanPlayerKt.isSpiderman(player) && this.getBlockStateAtPos().isOf(Blocks.COBWEB)) {
            return true;
        }
        return original;
    }
}
