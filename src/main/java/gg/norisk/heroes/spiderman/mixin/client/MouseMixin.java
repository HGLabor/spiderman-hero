package gg.norisk.heroes.spiderman.mixin.client;

import gg.norisk.heroes.spiderman.event.Events;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @ModifyArgs(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"))
    private void injected(Args args) {
        Events.INSTANCE.getMouseClickEvent().invoke(new Events.MouseClickEvent(args.get(0), args.get(1)));
    }
}
