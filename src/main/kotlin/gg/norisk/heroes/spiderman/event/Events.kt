package gg.norisk.heroes.spiderman.event

import net.minecraft.client.MinecraftClient
import net.minecraft.client.input.Input
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Event

//Hi Enricooe wenn du das hier list müssen wir auslagern also ist aber hatte kein template
@OptIn(ExperimentalSilkApi::class)
object Events {
    open class MouseClickEvent(val key: InputUtil.Key, val pressed: Boolean)
    open class AfterTickInputEvent(val input: Input)
    open class KeyEvent(val key: Int, val scanCode: Int, val action: Int, val client: MinecraftClient) {
        override fun toString(): String {
            return "KeyEvent(key=$key, scanCode=$scanCode, action=$action)"
        }

        fun isReleased(): Boolean = action == 0
        fun isClicked(): Boolean = action == 1
        fun isHold(): Boolean = action == 2

        fun matchesKeyBinding(keyBinding: KeyBinding): Boolean {
            return keyBinding.matchesKey(key, scanCode)
        }
    }


    val mouseClickEvent = Event.onlySync<MouseClickEvent>()
    val afterTickInputEvent = Event.onlySync<AfterTickInputEvent>()
    val keyEvent = Event.onlySync<KeyEvent>()
}
