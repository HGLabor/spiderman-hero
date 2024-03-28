package gg.norisk.heroes.spiderman.abilities

import gg.norisk.heroes.common.networking.Networking.mousePacket
import gg.norisk.heroes.common.networking.Networking.mouseScrollPacket
import gg.norisk.heroes.common.networking.dto.MousePacket
import gg.norisk.heroes.spiderman.entity.WebEntity.Companion.getWeb
import gg.norisk.heroes.spiderman.event.Events.entityCanClimbOnEvent
import gg.norisk.heroes.spiderman.player.isSpiderman
import gg.norisk.heroes.spiderman.player.playGenericSpidermanSound
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.silkmc.silk.network.packet.ServerPacketContext

object WebShooter {
    fun initServer() {
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

    private fun onMouseScrollPacket(isForward: Boolean, context: ServerPacketContext) {
        val player = context.player
        val web = player.getWeb() ?: return
        web.scale += 0.5f
    }

    private fun onMousePacket(mousePacket: MousePacket, context: ServerPacketContext) {
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
