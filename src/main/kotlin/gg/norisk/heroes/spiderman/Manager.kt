package gg.norisk.heroes.spiderman

import gg.norisk.heroes.spiderman.abilities.WebShooter
import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.entity.WebEntity.Companion.getWeb
import gg.norisk.heroes.spiderman.grapple.GrappleKey
import gg.norisk.heroes.spiderman.grapple.GrappleModUtils
import gg.norisk.heroes.spiderman.movement.LeadRenderer
import gg.norisk.heroes.spiderman.movement.Parabel
import gg.norisk.heroes.spiderman.movement.PendulumMovement
import gg.norisk.heroes.spiderman.movement.PullMovement
import gg.norisk.heroes.spiderman.network.MouseListener
import gg.norisk.heroes.spiderman.player.isSpiderman
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import gg.norisk.heroes.spiderman.registry.EntityRendererRegistry
import gg.norisk.heroes.spiderman.registry.ItemRegistry
import gg.norisk.heroes.spiderman.render.Ability
import gg.norisk.heroes.spiderman.render.AbilityRenderer
import gg.norisk.heroes.spiderman.render.Speedlines
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.world.Difficulty
import net.minecraft.world.GameRules
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.text.literal
import org.apache.logging.log4j.LogManager

object Manager : ModInitializer, DedicatedServerModInitializer, ClientModInitializer {
    val logger = LogManager.getLogger("spiderman-hero")
    var fovMultiplier = true
    var soundEffect = true
    var cameraOffset = true
    var speedlines = true

    override fun onInitialize() {
        // Common initialization
        ItemRegistry.init()
        EntityRegistry.init()
        PendulumMovement.initialize()
        LeadRenderer.init()
        PullMovement.init()
        Parabel.init()

        WebShooter.initServer()
        WebShooter.initCommon()
        WebEntity.initServer()

        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
            if (FabricLoader.getInstance().isDevelopmentEnvironment) {
                it.setDifficulty(Difficulty.PEACEFUL, true)
                it.overworld.timeOfDay = 6000
                it.gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, it)
                it.gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, it)
            }
        })

        command("spiderman") {
            runs {
                val player = this.source.playerOrThrow
                player.isSpiderman = !player.isSpiderman
            }
        }

        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            command("visuell") {
                literal("fovmultiplier") {
                    runs {
                        fovMultiplier = !fovMultiplier
                    }
                }
                literal("soundeffect") {
                    runs {
                        soundEffect = !soundEffect
                    }
                }
                literal("cameraoffset") {
                    runs {
                        cameraOffset = !cameraOffset
                    }
                }
                literal("speedlines") {
                    runs {
                        speedlines = !speedlines
                        this.source.sendMessage("Speedlines: $speedlines".literal)
                    }
                }
            }
        }
    }

    override fun onInitializeClient() {
        // Client initialization
        EntityRendererRegistry.init()
        GrappleKey.registerAll()
        GrappleModUtils.init()
        Speedlines.initClient()
        WebShooter.initClient()
        MouseListener.initClient()
        AbilityRenderer.init()

        ClientLifecycleEvents.CLIENT_STARTED.register {
            AbilityRenderer.abilities += Ability(WebShooter.webShooterKey, { it.isSpiderman }, { "Web Shooter" })
            AbilityRenderer.abilities += Ability(MinecraftClient.getInstance().options.pickItemKey, {
                return@Ability it.getWeb() != null && it.isSpiderman
            }, { "Netzgröße" }, keyText = "Scroll"
            )
            AbilityRenderer.abilities += Ability(
                MinecraftClient.getInstance().options.pickItemKey,
                {
                    return@Ability it.getWeb() != null && it.isSpiderman
                },
                { "Spinnennetz" },
            )
            AbilityRenderer.abilities += Ability(
                MinecraftClient.getInstance().options.attackKey,
                {
                    val web = it.getWeb() ?: return@Ability false
                    return@Ability web.hasVehicle() && it.isSpiderman
                },
                { "Ziehen" },
            )
            AbilityRenderer.abilities += Ability(
                MinecraftClient.getInstance().options.useKey,
                {
                    val web = it.getWeb() ?: return@Ability false
                    return@Ability web.hasVehicle() && it.isSpiderman
                },
                { "Grapple" },
            )
            AbilityRenderer.abilities += Ability(
                MinecraftClient.getInstance().options.jumpKey,
                {
                    val web = it.getWeb() ?: return@Ability false
                    return@Ability web.isCollided && it.isSpiderman
                },
                { "Netz Länger" },
            )
            AbilityRenderer.abilities += Ability(
                MinecraftClient.getInstance().options.sneakKey,
                {
                    val web = it.getWeb() ?: return@Ability false
                    return@Ability web.isCollided && it.isSpiderman
                },
                { "Netz Kürzer" },
            )
            AbilityRenderer.abilities += Ability(MinecraftClient.getInstance().options.jumpKey, {
                val web = it.getWeb() ?: return@Ability false
                return@Ability web.isCollided && it.isSpiderman
            }, { "Absprung" }, prefix = "2x ".literal
            )
        }

    }

    override fun onInitializeServer() {
        // Dedicated server initialization
    }

    fun String.toId() = Identifier("examplemod", this)
}
