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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow
    protected abstract boolean isCamera();

    @Shadow
    public Input input;

    public ClientPlayerEntityMixin(ClientWorld clientWorld, GameProfile gameProfile) {
        super(clientWorld, gameProfile);
    }

    @Inject(method = "tickNewAi", at = @At("TAIL"))
    private void tickNewAiInjection(CallbackInfo ci) {
        if (this.isCamera()) {
            if (SpidermanPlayerKt.isSwinging(this)) {
                this.forwardSpeed = 0;
                this.sidewaysSpeed = 0f;
            }
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick(ZF)V", shift = At.Shift.AFTER))
    public void inputHandle(CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (!MinecraftClient.getInstance().isRunning() || player == null) return;
        Events.INSTANCE.getAfterTickInputEvent().invoke(new Events.AfterTickInputEvent(this.input));
    }
}
