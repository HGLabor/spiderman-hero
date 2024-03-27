package gg.norisk.heroes.spiderman.abilities

import gg.norisk.heroes.spiderman.entity.WebEntity.Companion.getWeb
import gg.norisk.heroes.spiderman.network.MouseListener
import gg.norisk.heroes.spiderman.network.MouseListener.mousePacket
import gg.norisk.heroes.spiderman.player.playGenericSpidermanSound
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.network.packet.ServerPacketContext

object WebShooter {
    fun initServer() {
        mousePacket.receiveOnServer(::onMousePacket)
    }

    fun initClient() {

    }

    private fun onMousePacket(mousePacket: MouseListener.MousePacket, context: ServerPacketContext) {
        val player = context.player
        player.sendMessage(mousePacket.toString().literal)

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
