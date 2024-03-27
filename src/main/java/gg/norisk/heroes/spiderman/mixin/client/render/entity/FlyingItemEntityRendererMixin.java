package gg.norisk.heroes.spiderman.mixin.client.render.entity;

import gg.norisk.heroes.spiderman.entity.WebEntity;
import gg.norisk.heroes.spiderman.movement.LeadRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FlyingItemEntityRenderer.class)
public abstract class FlyingItemEntityRendererMixin<T extends Entity & FlyingItemEntity> extends EntityRenderer<T> {
    protected FlyingItemEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderInjection(T entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (entity instanceof WebEntity webEntity && webEntity.getOwner() instanceof PlayerEntity owner) {
            LeadRenderer.INSTANCE.renderLeash(g, matrixStack, vertexConsumerProvider, webEntity, owner);
        }
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"))
    private void injected(Args args, T entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (entity instanceof WebEntity webEntity) {
            args.setAll(webEntity.getScale(), webEntity.getScale(), webEntity.getScale());
        }
    }
}
