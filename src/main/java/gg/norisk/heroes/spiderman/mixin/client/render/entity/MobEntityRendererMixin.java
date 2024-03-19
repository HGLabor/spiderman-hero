package gg.norisk.heroes.spiderman.mixin.client.render.entity;

import gg.norisk.heroes.spiderman.movement.LeadRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntityRenderer.class)
public abstract class MobEntityRendererMixin<T extends MobEntity, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
    @Shadow
    private static void renderLeashPiece(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, int i, int j, int k, int l, float m, float n, float o, float p, int q, boolean bl) {
    }

    @Unique
    private boolean isSpidermanLeash = false;

    public MobEntityRendererMixin(EntityRendererFactory.Context context, M entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "renderLeash", at = @At("HEAD"))
    private <E extends Entity> void renderLeashInjection(T mobEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, E entity, CallbackInfo ci) {
        this.isSpidermanLeash = LeadRenderer.INSTANCE.checkForSpidermanLeash(mobEntity, entity);
    }

    @Inject(method = "shouldRender(Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/client/render/Frustum;DDD)Z", at = @At("RETURN"), cancellable = true)
    private void shouldRenderInjection(T mobEntity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = mobEntity.getHoldingEntity();
        var shouldRender = cir.getReturnValue();
        if (entity != null && !shouldRender) {
            cir.setReturnValue(LeadRenderer.INSTANCE.checkForSpidermanLeash(mobEntity, entity));
        }
    }

    @Redirect(method = "renderLeash", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/MobEntityRenderer;renderLeashPiece(Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;FFFIIIIFFFFIZ)V"))
    private void injected(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, int i, int j, int k, int l, float m, float n, float o, float p, int q, boolean bl) {
        if (isSpidermanLeash) {
            renderSpiderLeash(vertexConsumer, matrix4f, f, g, h, i, j, k, l, m, n, o, p, q, bl);
        } else {
            renderLeashPiece(vertexConsumer, matrix4f, f, g, h, i, j, k, l, m, n, o, p, q, bl);
        }
    }

    @Unique
    private static void renderSpiderLeash(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, int i, int j, int k, int l, float m, float n, float o, float p, int q, boolean bl) {
        float r = (float) q / 24.0F;
        int s = (int) MathHelper.lerp(r, (float) i, (float) j);
        int t = (int) MathHelper.lerp(r, (float) k, (float) l);
        int u = LightmapTextureManager.pack(s, t);
        float v = q % 2 == (bl ? 1 : 0) ? 0.7F : 1.0F;
        float w = 1f * v;
        float x = 1f * v;
        float y = 1f * v;
        float z = f * r;
        float aa = g > 0.0F ? g * r * r : g - g * (1.0F - r) * (1.0F - r);
        float ab = h * r;
        vertexConsumer.vertex(matrix4f, z - o, aa + n, ab + p).color(w, x, y, 1.0F).light(u).next();
        vertexConsumer.vertex(matrix4f, z + o, aa + m - n, ab - p).color(w, x, y, 1.0F).light(u).next();
    }
}
