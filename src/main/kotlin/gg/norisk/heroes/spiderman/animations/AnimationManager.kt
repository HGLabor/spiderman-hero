package gg.norisk.heroes.spiderman.animations

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier
import dev.kosmx.playerAnim.core.util.Ease
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry
import gg.norisk.heroes.spiderman.Manager.toId
import gg.norisk.heroes.spiderman.player.IAnimatedPlayer
import kotlinx.serialization.ExperimentalSerializationApi
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.Perspective
import net.minecraft.entity.player.PlayerEntity
import net.silkmc.silk.network.packet.c2sPacket
import net.silkmc.silk.network.packet.s2cPacket

@OptIn(ExperimentalSerializationApi::class)
object AnimationManager {
    private val c2sAnimationStart = c2sPacket<AnimationPacket>("play-animation".toId())
    private val s2cAnimationStart = s2cPacket<AnimationPacket>("play-animation".toId())
    private val s2cAnimationReset = s2cPacket<ResetAnimationPacket>("reset-animation".toId())

    fun init() {
        c2sAnimationStart.receiveOnServer { packet, context ->
            if (context.player.uuid != packet.playerUuid) return@receiveOnServer
            s2cAnimationStart.sendToAll(packet)
        }

        s2cAnimationReset.receiveOnClient { packet, context ->
            val worldPlayers = MinecraftClient.getInstance().world?.players ?: return@receiveOnClient
            worldPlayers.forEach(AnimationManager::resetAnimation)
        }

        s2cAnimationStart.receiveOnClient { packet, _ ->
            val worldPlayers = MinecraftClient.getInstance().world?.players ?: return@receiveOnClient
            val player = worldPlayers.firstOrNull {
                it.uuid == packet.playerUuid
            } ?: return@receiveOnClient
            playAnimation(player, packet.animation)
        }
    }

    fun broadcastResetAnimation(player: PlayerEntity) {
        val packet = ResetAnimationPacket(player.uuid)
        s2cAnimationReset.sendToAll(packet)
    }

    fun resetAnimation(player: PlayerEntity) {
        val animationContainer = (player as IAnimatedPlayer).hero_getModAnimation()
        animationContainer.animation = null
    }

    fun broadcastAnimation(player: PlayerEntity, animation: String) {
        val packet = AnimationPacket(player.uuid, animation)
        c2sAnimationStart.send(packet)
    }

    fun playAnimation(player: PlayerEntity, animation: String) {
        val animationContainer = (player as IAnimatedPlayer).hero_getModAnimation()
        var anim = PlayerAnimationRegistry.getAnimation(animation.toId())
            ?: error("No animation found for: ${animation.toId()}")

        //TODO das muss geiler gemacht werden ist jetzt nur für Darth Vader
        val builder = anim.mutableCopy()
        anim = builder.build()

        if (animation.equals("spiderman")) {
            MinecraftClient.getInstance().options.perspective = Perspective.THIRD_PERSON_FRONT
        }

        //TODO das muss geiler gemacht werden ist jetzt nur für Darth Vader in THird Person
        animationContainer.replaceAnimationWithFade(
            AbstractFadeModifier.standardFadeIn(3, Ease.INBOUNCE),
            NamedKeyframeAnimationPlayer(animation, anim).setFirstPersonMode(
               if (animation.startsWith("sword")) FirstPersonMode.THIRD_PERSON_MODEL else FirstPersonMode.VANILLA
            )
        )

    }
}
