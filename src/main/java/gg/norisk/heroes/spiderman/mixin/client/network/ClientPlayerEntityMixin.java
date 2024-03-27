package gg.norisk.heroes.spiderman.mixin.client.network;

import com.mojang.authlib.GameProfile;
import gg.norisk.heroes.spiderman.event.Events;
import gg.norisk.heroes.spiderman.player.SpidermanPlayerKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow
    public Input input;

    @Shadow
    @Final
    protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld clientWorld, GameProfile gameProfile) {
        super(clientWorld, gameProfile);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick(ZF)V", shift = At.Shift.AFTER))
    public void inputHandle(CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (!MinecraftClient.getInstance().isRunning() || player == null) return;
        Events.INSTANCE.getAfterTickInputEvent().invoke(new Events.AfterTickInputEvent(this.input));
    }

    @Inject(method = "getLeashPos", at = @At("RETURN"), cancellable = true)
    private void getLeashPosInjection(float f, CallbackInfoReturnable<Vec3d> cir) {
        /*if (SpidermanPlayerKt.isSwinging(this)) {
            if (this.client.options.getPerspective().isFirstPerson()) {
                float g = MathHelper.lerp(f * 0.5F, this.getYaw(), this.prevYaw) * (float) (Math.PI / 180.0);
                float h = MathHelper.lerp(f * 0.5F, this.getPitch(), this.prevPitch) * (float) (Math.PI / 180.0);
                double d = this.getMainArm() == Arm.RIGHT ? -1.0 : 1.0;
                Vec3d vec3d = new Vec3d(0.39 * d, 0.0, 0.3);
                cir.setReturnValue(vec3d.rotateX(-h).rotateY(-g).add(this.getCameraPosVec(f)));
            } else {
                float h = MathHelper.lerp(f, this.prevBodyYaw, this.bodyYaw) * (float) (Math.PI / 180.0);

                double m = this.getBoundingBox().getLengthY() + 0.2;
                double e = this.isInSneakingPose() ? -0.2 : 0.07;
                var pos = this.getLerpedPos(f).add(new Vec3d(0, m, e).rotateY(-h));

                cir.setReturnValue(pos);
            }
        }*/
    }
}
