package gg.norisk.heroes.spiderman.abilities

import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.entity.WebEntity.Companion.getWeb
import gg.norisk.heroes.spiderman.event.Events
import gg.norisk.heroes.spiderman.event.Events.entityCanClimbOnEvent
import gg.norisk.heroes.spiderman.event.Events.keyEvent
import gg.norisk.heroes.spiderman.network.MouseListener
import gg.norisk.heroes.spiderman.network.MouseListener.mousePacket
import gg.norisk.heroes.spiderman.network.MouseListener.mouseScrollPacket
import gg.norisk.heroes.spiderman.network.Packets.webShooterPacketC2S
import gg.norisk.heroes.spiderman.player.isSpiderman
import gg.norisk.heroes.spiderman.player.playGenericSpidermanSound
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.block.Blocks
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.network.packet.ServerPacketContext
import org.lwjgl.glfw.GLFW

object WebShooter {
    val webShooterKey = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.spiderman.webshooter",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.spiderman.abilities"
        )
    )

    fun initServer() {
        webShooterPacketC2S.receiveOnServer(::onWebShooterPacket)
        mousePacket.receiveOnServer(::onMousePacket)
        mouseScrollPacket.receiveOnServer(::onMouseScrollPacket)
    }

    fun initCommon() {
        entityCanClimbOnEvent.listen {
            val player = it.entity as? PlayerEntity ?: return@listen
            it.callBack.returnValue =
                it.callBack.returnValue || (it.blockState.isOf(Blocks.COBWEB) && player.isSpiderman)
        }
    }

    @OptIn(ExperimentalSilkApi::class)
    fun initClient() {
        keyEvent.listen { onKeyEvent(it) }
    }

    private fun onKeyEvent(event: Events.KeyEvent) {
        if (event.matchesKeyBinding(webShooterKey) && event.isClicked()) {
            webShooterPacketC2S.send(Unit)
        }
    }

    private fun onWebShooterPacket(webShooterPacket: Unit, context: ServerPacketContext) {
        val player = context.player
        val world = player.serverWorld

        WebEntity.removeWebs(player)

        player.playGenericSpidermanSound()
        val webEntity = WebEntity(world, player)
        webEntity.setItem(Items.COBWEB.defaultStack)
        webEntity.setVelocity(player, player.pitch, player.yaw, 0.0f, 2f, 1.0f)
        world.spawnEntity(webEntity)
    }

    private fun onMouseScrollPacket(isForward: Boolean, context: ServerPacketContext) {
        val player = context.player
        val web = player.getWeb() ?: return
        web.scale += 0.5f
    }

    private fun onMousePacket(mousePacket: MouseListener.MousePacket, context: ServerPacketContext) {
        val player = context.player

        when {
            mousePacket.isLeft() && mousePacket.isClicked() -> {
                val web = player.getWeb()
                if (web != null && web.hasVehicle()) {
                    player.playGenericSpidermanSound()
                    web.pullVehicleTowardsOwner(5.0)
                }
            }

            mousePacket.isRight() && mousePacket.isClicked() -> {
                val web = player.getWeb()
                if (web != null && web.hasVehicle()) {
                    player.playGenericSpidermanSound()
                    web.pullEntityTowardsWeb(player, 3.0)
                }
            }

            mousePacket.isMiddle() && mousePacket.isClicked() -> {
                val web = player.getWeb()
                if (web != null) {
                    player.playGenericSpidermanSound()
                    web.convertToCobwebs()
                }
            }

            mousePacket.isHoldingLeftClick() -> {
                val web = player.getWeb()
                if (web != null && web.hasVehicle()) {
                    web.pullVehicleTowardsOwner()
                }
            }

            mousePacket.isHoldingRightClick() -> {
                val web = player.getWeb()
                if (web != null && web.hasVehicle()) {
                    web.pullEntityTowardsWeb(player)
                }
            }
        }
    }
}
