package gg.norisk.heroes.spiderman.mixin.client.render;

import gg.norisk.heroes.spiderman.Manager;
import gg.norisk.heroes.spiderman.player.IAnimatedPlayer;
import gg.norisk.heroes.spiderman.player.SpidermanPlayerKt;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void moveBy(double d, double e, double f);

    @Shadow
    protected abstract double clipToSpace(double d);

    @Shadow
    private float lastCameraY;
    @Shadow
    private float cameraY;
    @Unique
    private double currentCameraX;
    @Unique
    private double currentCameraY;
    @Unique
    private double currentCameraZ;
    @Unique
    private double lerpedLength;

    @Unique
    private double lerpedSpeed;


    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPosInjection(Args args, BlockView blockView, Entity entity, boolean bl, boolean bl2, float delta) {
        delta *= 0.005f;
        float velocity = Math.min(1, (float) (1 / entity.getVelocity().length()) / 20) * 0.5f;
        lerpedSpeed = MathHelper.lerp((delta), lerpedSpeed, entity.getVelocity().length());
        //float lerpedVelocity = (float) (1 / lerpedSpeed) / 5;

        //System.out.println("Velocity:  + " + velocity);

        //System.out.println("Lerped Velocity " + velocity);
        //f = velocity;
        //f = velocity;

        currentCameraX = MathHelper.lerp(velocity, currentCameraX, entity.getX());
        currentCameraY = MathHelper.lerp(velocity, currentCameraY, args.get(1));
        currentCameraZ = MathHelper.lerp(velocity, currentCameraZ, entity.getZ());
        //args.set(0, currentCameraX);
        //args.set(1,currentCameraY);
        //args.set(2, currentCameraZ);
        //args.setAll(currentCameraX, currentCameraY, currentCameraZ);
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V", ordinal = 0))
    private void setPosInjection2(Args args, BlockView blockView, Entity entity, boolean bl, boolean bl2, float delta) {
        var velocity = entity.getVelocity().length();
        var length = 2.0 + velocity * 3;
        lerpedLength = MathHelper.lerp((delta * 0.2), lerpedLength, length);
        if (entity instanceof PlayerEntity player && SpidermanPlayerKt.isSpiderman(player)) {
            if (Manager.INSTANCE.getCameraOffset() && ((IAnimatedPlayer) player).hero_getModAnimation().getAnimation() == null
            ) {
                args.set(0, -this.clipToSpace(lerpedLength));
            }
        }
        //TODO geilen wert finden TODO
    }
}
