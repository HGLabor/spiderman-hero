package gg.norisk.heroes.spiderman.mixin.client.gl;

import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PostEffectProcessor.class)
public interface PostEffectProcessorAccessor {

    @Accessor
    List<PostEffectPass> getPasses();
}
