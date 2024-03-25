package gg.norisk.heroes.spiderman.event

import net.minecraft.client.input.Input
import net.minecraft.client.util.InputUtil
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Event

//Hi Enricooe wenn du das hier list müssen wir auslagern also ist aber hatte kein template
@OptIn(ExperimentalSilkApi::class)
object Events {
    open class MouseClickEvent(val key: InputUtil.Key, val pressed: Boolean)
    open class AfterTickInputEvent(val input: Input)

    val mouseClickEvent = Event.onlySync<MouseClickEvent>()
    val afterTickInputEvent = Event.onlySync<AfterTickInputEvent>()
}
