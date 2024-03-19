package gg.norisk.heroes.spiderman.mixin.entity;

import gg.norisk.heroes.spiderman.player.SpidermanPlayerKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.08))
    private double injected(double constant) {
        if ((Object) this instanceof PlayerEntity player) {
            return SpidermanPlayerKt.getGravity(player);
        }
        return constant;
    }
}
