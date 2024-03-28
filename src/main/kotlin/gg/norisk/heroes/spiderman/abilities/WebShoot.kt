package gg.norisk.heroes.spiderman.abilities

import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.dto.MousePacket
import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.entity.WebEntity.Companion.getWeb
import gg.norisk.heroes.spiderman.player.playGenericSpidermanSound
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.network.packet.ServerPacketContext
import org.lwjgl.glfw.GLFW

val WebShoot by PressAbility("WebShoot", 20) {

    //TODO GENERISCH
    if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
        keyBindCode = GLFW.GLFW_KEY_G
    }

    handle {
        server { player, description ->
            val world = (player as ServerPlayerEntity).serverWorld

            player.sendMessage("PRessed WebShoot".literal)

            WebEntity.removeWebs(player)

            player.playGenericSpidermanSound()
            val webEntity = WebEntity(world, player)
            webEntity.setItem(Items.COBWEB.defaultStack)
            webEntity.setVelocity(player, player.pitch, player.yaw, 0.0f, 2f, 1.0f)
            world.spawnEntity(webEntity)
        }
    }


    fun onMouseScrollPacket(isForward: Boolean, context: ServerPacketContext) {
        val player = context.player
        val web = player.getWeb() ?: return
        web.scale += 0.5f
    }

    fun onMousePacket(mousePacket: MousePacket, context: ServerPacketContext) {
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


    Networking.mousePacket.receiveOnServer(::onMousePacket)
    Networking.mouseScrollPacket.receiveOnServer(::onMouseScrollPacket)
}
