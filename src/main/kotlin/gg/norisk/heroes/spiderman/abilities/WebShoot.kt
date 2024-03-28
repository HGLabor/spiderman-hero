package gg.norisk.heroes.spiderman.abilities

import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.player.playGenericSpidermanSound
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.text.literal
import org.lwjgl.glfw.GLFW

val WebShoot by PressAbility("WebShoot", 20) {

    //TODO GENERISCH
    keyBind = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.spiderman.webshooter",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.spiderman.abilities"
        )
    )

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
