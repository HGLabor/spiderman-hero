package gg.norisk.heroes.spiderman.mixin.entity;

import gg.norisk.heroes.spiderman.event.Events;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "canClimb", at = @At("RETURN"), cancellable = true)
    private void injected(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        Events.INSTANCE.getEntityCanClimbOnEvent().invoke(new Events.EntityCanClimbOnEvent((Entity) (Object) this, blockState, cir));
    }
}
