package gg.norisk.heroes.spiderman.event

import gg.norisk.heroes.spiderman.Manager.toId
import kotlinx.serialization.ExperimentalSerializationApi
import net.minecraft.client.MinecraftClient
import net.minecraft.client.input.Input
import net.minecraft.client.util.InputUtil
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Event
import net.silkmc.silk.network.packet.c2sPacket

//Hi Enricooe wenn du das hier list müssen wir auslagern also ist aber hatte kein template
@OptIn(ExperimentalSilkApi::class)
object Events {
    open class MouseClickEvent(val key: InputUtil.Key, val pressed: Boolean)
    open class AfterTickInputEvent(val input: Input)
    open class KeyEvent(val key: Int, val scanCode: Int, val action: Int, val client: MinecraftClient) {
        override fun toString(): String {
            return "KeyEvent(key=$key, scanCode=$scanCode, action=$action)"
        }
    }


    val mouseClickEvent = Event.onlySync<MouseClickEvent>()
    val afterTickInputEvent = Event.onlySync<AfterTickInputEvent>()
    val keyEvent = Event.onlySync<KeyEvent>()
}
