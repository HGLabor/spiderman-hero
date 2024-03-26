package gg.norisk.heroes.spiderman.mixin.client.render;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @ModifyConstant(method = "updateFovMultiplier", constant = @Constant(floatValue = 1.5f))
    private float injected(float constant) {
        return 10f;
    }
}
