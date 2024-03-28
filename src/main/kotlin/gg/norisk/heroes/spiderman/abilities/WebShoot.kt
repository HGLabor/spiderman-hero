package gg.norisk.heroes.spiderman.abilities

import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.player.playGenericSpidermanSound
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.text.literal
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
}
