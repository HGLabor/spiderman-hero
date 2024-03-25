package gg.norisk.heroes.spiderman.grapple;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

/*
Full Credits to https://github.com/yyon/grapplemod
 */
public class GrappleKey {

    public static ArrayList<KeyBinding> keyBindings = new ArrayList<>();

    public static KeyBinding createKeyBinding(KeyBinding k) {
        keyBindings.add(k);
        return k;
    }

    public static final KeyBinding THROW_HOOKS = GrappleKey.createKeyBinding(new KeyBinding("key.boththrow.desc", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_2, "key.grapplemod.category"));
    public static final KeyBinding THROW_LEFT_HOOK = GrappleKey.createKeyBinding(new KeyBinding("key.leftthrow.desc", InputUtil.UNKNOWN_KEY.getCode(), "key.grapplemod.category"));
    public static final KeyBinding THROW_RIGHT_HOOK = GrappleKey.createKeyBinding(new KeyBinding("key.rightthrow.desc", InputUtil.UNKNOWN_KEY.getCode(), "key.grapplemod.category"));
    public static final KeyBinding TOGGLE_MOTOR = GrappleKey.createKeyBinding(new KeyBinding("key.motoronoff.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.grapplemod.category"));
    public static final KeyBinding DETACH = GrappleKey.createKeyBinding(new KeyBinding("key.jumpanddetach.desc", GLFW.GLFW_KEY_SPACE, "key.grapplemod.category"));
    public static final KeyBinding DAMPEN_SWING = GrappleKey.createKeyBinding(new KeyBinding("key.slow.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.grapplemod.category"));
    public static final KeyBinding CLIMB = GrappleKey.createKeyBinding(new KeyBinding("key.climb.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.grapplemod.category"));
    public static final KeyBinding CLIMB_UP = GrappleKey.createKeyBinding(new KeyBinding("key.climbup.desc", InputUtil.UNKNOWN_KEY.getCode(), "key.grapplemod.category"));
    public static final KeyBinding CLIMB_DOWN = GrappleKey.createKeyBinding(new KeyBinding("key.climbdown.desc", InputUtil.UNKNOWN_KEY.getCode(), "key.grapplemod.category"));
    public static final KeyBinding HOOK_ENDER_LAUNCH = GrappleKey.createKeyBinding(new KeyBinding("key.enderlaunch.desc", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_1, "key.grapplemod.category"));
    public static final KeyBinding ROCKET = GrappleKey.createKeyBinding(new KeyBinding("key.rocket.desc", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_1, "key.grapplemod.category"));
    public static final KeyBinding SLIDE = GrappleKey.createKeyBinding(new KeyBinding("key.slide.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.grapplemod.category"));


    public static void registerAll() {
        for (KeyBinding mapping : GrappleKey.keyBindings) {
            KeyBindingHelper.registerKeyBinding(mapping);
        }
    }
}
