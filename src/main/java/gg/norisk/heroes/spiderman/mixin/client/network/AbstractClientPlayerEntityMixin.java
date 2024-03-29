package gg.norisk.heroes.spiderman.mixin.client.network;

import com.mojang.authlib.GameProfile;
import gg.norisk.heroes.spiderman.Manager;
import gg.norisk.heroes.spiderman.player.SpidermanPlayerKt;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {
    public AbstractClientPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @ModifyArgs(method = "getFovMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void injected(Args args) {
        if (!Manager.INSTANCE.getFovMultiplier()) return;
        if (SpidermanPlayerKt.isSpiderman(this)) {
            float f = args.get(2);

            //TODO anpassen
            if (this.getVelocity().length() > 0.5) {
                f *= (float) Math.min(1.5, this.getVelocity().length() * 2);
            }

            args.set(2, f);
        }
    }

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void injected(CallbackInfoReturnable<SkinTextures> cir) {
        if (SpidermanPlayerKt.isSpiderman(this)) {
            var skinTexture = cir.getReturnValue();
            //Identifier comp_1626,
            //	@Nullable String comp_1911,
            //	@Nullable Identifier comp_1627,
            //	@Nullable Identifier comp_1628,
            //	SkinTextures.Model comp_1629,
            //	boolean comp_1630
            cir.setReturnValue(new SkinTextures(Manager.INSTANCE.getSpidermanSkin(), skinTexture.comp_1911(), skinTexture.comp_1627(), skinTexture.comp_1628(), skinTexture.comp_1629(), skinTexture.comp_1630()));
        }
    }
}
