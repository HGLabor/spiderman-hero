package gg.norisk.heroes.spiderman.mixin.entity.player;

import gg.norisk.heroes.spiderman.player.SpidermanPlayer;
import gg.norisk.heroes.spiderman.player.SpidermanPlayerKt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.OptionalInt;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements SpidermanPlayer {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTrackerInjection(CallbackInfo ci) {
        this.dataTracker.startTracking(SpidermanPlayerKt.getSpidermanAnchorPointTracker(), Optional.empty());
        this.dataTracker.startTracking(SpidermanPlayerKt.getGravityTracker(), (float) LivingEntity.GRAVITY);
        this.dataTracker.startTracking(SpidermanPlayerKt.getSwingingTracker(), false);
        this.dataTracker.startTracking(SpidermanPlayerKt.getLeashEntityIdTracker(), OptionalInt.empty());
    }
}
