package gg.norisk.heroes.spiderman

import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.grapple.GrappleKey
import gg.norisk.heroes.spiderman.grapple.GrappleModUtils
import gg.norisk.heroes.spiderman.movement.LeadRenderer
import gg.norisk.heroes.spiderman.movement.Parabel
import gg.norisk.heroes.spiderman.movement.PendulumMovement
import gg.norisk.heroes.spiderman.movement.PullMovement
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import gg.norisk.heroes.spiderman.registry.EntityRendererRegistry
import gg.norisk.heroes.spiderman.registry.ItemRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier
import net.minecraft.world.Difficulty
import net.minecraft.world.GameRules
import org.apache.logging.log4j.LogManager

object Manager : ModInitializer, DedicatedServerModInitializer, ClientModInitializer {
    val logger = LogManager.getLogger("spiderman-hero")

    override fun onInitialize() {
        // Common initialization
        ItemRegistry.init()
        EntityRegistry.init()
        PendulumMovement.initialize()
        LeadRenderer.init()
        PullMovement.init()
        Parabel.init()

        WebEntity.initServer()

        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
            if (FabricLoader.getInstance().isDevelopmentEnvironment) {
                it.setDifficulty(Difficulty.PEACEFUL, true)
                it.overworld.timeOfDay = 6000
                it.gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, it)
                it.gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, it)
            }
        })
    }

    override fun onInitializeClient() {
        // Client initialization
        EntityRendererRegistry.init()
        GrappleKey.registerAll()
        GrappleModUtils.init()
    }

    override fun onInitializeServer() {
        // Dedicated server initialization
    }

    fun String.toId() = Identifier("examplemod", this)
}
