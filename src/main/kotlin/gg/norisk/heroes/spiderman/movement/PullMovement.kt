package gg.norisk.heroes.spiderman.movement

import gg.norisk.heroes.spiderman.event.Events
import net.minecraft.client.MinecraftClient
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import java.util.*

@OptIn(ExperimentalSilkApi::class)
object PullMovement {
    //TODO maybe als datatracker für andere helden auch?
    var isPulling = mutableSetOf<UUID>()

    fun init() {
        Events.mouseClickEvent.listen { onMouseClick(it) }
    }

    fun onMouseClick(mouseClickEvent: Events.MouseClickEvent) {
        val player = MinecraftClient.getInstance().player ?: return
        if (MinecraftClient.getInstance().options.attackKey.matchesMouse(mouseClickEvent.key.code)) {
            if (mouseClickEvent.pressed) {
                isPulling.add(player.uuid)
            } else {
                isPulling.remove(player.uuid)
            }
        }
    }
}
