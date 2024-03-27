package gg.norisk.heroes.spiderman.network

import gg.norisk.heroes.spiderman.Manager.toId
import gg.norisk.heroes.spiderman.event.Events.mouseClickEvent
import gg.norisk.heroes.spiderman.event.Events.mouseScrollEvent
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.silkmc.silk.network.packet.c2sPacket

object MouseListener {
    enum class Type {
        LEFT, MIDDLE, RIGHT
    }

    enum class Action {
        CLICK, RELEASE, HOLD
    }

    @Serializable
    data class MousePacket(val type: Type, val action: Action) {
        fun isLeft(): Boolean = type == Type.LEFT
        fun isRight(): Boolean = type == Type.RIGHT
        fun isMiddle(): Boolean = type == Type.MIDDLE

        fun isHolding(): Boolean = action == Action.HOLD
        fun isReleased(): Boolean = action == Action.RELEASE
        fun isClicked(): Boolean = action == Action.CLICK

        fun isHoldingLeftClick(): Boolean = isLeft() && isHolding()
        fun isHoldingRightClick(): Boolean = isRight() && isHolding()
        fun isHoldingMiddleClick(): Boolean = isMiddle() && isHolding()

        override fun toString(): String {
            return "[$type, $action]"
        }
    }

    val mousePacket = c2sPacket<MousePacket>("mouse-packet".toId())
    val mouseScrollPacket = c2sPacket<Boolean>("mouse-scroll".toId())

    fun init() {

    }

    fun initClient() {
        mouseScrollEvent.listen {
            MinecraftClient.getInstance().player ?: return@listen
            mouseScrollPacket.send(it.vertical > 0)
        }
        mouseClickEvent.listen {
            MinecraftClient.getInstance().player ?: return@listen
            if (MinecraftClient.getInstance().options.attackKey.matchesMouse(it.key.code)) {
                mousePacket.send(MousePacket(Type.LEFT, if (it.pressed) Action.CLICK else Action.RELEASE))
            } else if (MinecraftClient.getInstance().options.useKey.matchesMouse(it.key.code)) {
                mousePacket.send(MousePacket(Type.RIGHT, if (it.pressed) Action.CLICK else Action.RELEASE))
            } else if (MinecraftClient.getInstance().options.pickItemKey.matchesMouse(it.key.code)) {
                mousePacket.send(MousePacket(Type.MIDDLE, if (it.pressed) Action.CLICK else Action.RELEASE))
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            MinecraftClient.getInstance().player ?: return@register
            if (MinecraftClient.getInstance().options.attackKey.isPressed) {
                mousePacket.send(MousePacket(Type.LEFT, Action.HOLD))
            }
            if (MinecraftClient.getInstance().options.useKey.isPressed) {
                mousePacket.send(MousePacket(Type.RIGHT, Action.HOLD))
            }
            if (MinecraftClient.getInstance().options.pickItemKey.isPressed) {
                mousePacket.send(MousePacket(Type.MIDDLE, Action.HOLD))
            }
        }
    }
}
