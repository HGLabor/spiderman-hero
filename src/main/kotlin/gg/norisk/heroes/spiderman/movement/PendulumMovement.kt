package gg.norisk.heroes.spiderman.movement

import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.player.isSwinging
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.SpawnReason
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.text.literal
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PendulumMovement : ServerTickEvents.EndTick {
    data class PlayerSwingData(
        var swingAnchor: Vec3d,
        var startTime: Double = 0.0,
        var forwardMultiplier: Double = 1.0,
        var lateralMultiplier: Double = 0.0 // Neuer Multiplikator für die seitliche Bewegung
    )

    val playerSwingDataMap = mutableMapOf<UUID, PlayerSwingData>()

    // Der Ankerpunkt für das Schwingen

    // Zeit in Sekunden seit Start des Schwingens

    // Zeit-Update-Rate (Annahme: Server Tick Rate = 20 Ticks pro Sekunde)
    private val deltaTime = 1.0 / 20.0

    fun initialize() {
        command("toggleswinging") {
            runs {
                this.source.playerOrThrow.isSwinging = !this.source.playerOrThrow.isSwinging
            }
        }
        command("swing") {
            runs {
                val player = this.source.playerOrThrow
                if (playerSwingDataMap.containsKey(player.uuid)) {
                    playerSwingDataMap.remove(player.uuid)
                    player.serverWorld.iterateEntities().filterIsInstance<WebEntity>().forEach(Entity::discard)
                    player.isSwinging = false
                    player.sendMessage("Stopped swinging.".literal)
                } else {
                    // Setze einen neuen Ankerpunkt und Startzeit für den Spieler
                    val swingAnchor = player.blockPos.add(0, 10, 0)
                    val world = player.serverWorld
                    player.isSwinging = true

                    player.world.setBlockState(
                        BlockPos(
                            swingAnchor.x,
                            swingAnchor.y,
                            swingAnchor.z
                        ),
                        Blocks.BEDROCK.defaultState
                    )

                    val web = EntityRegistry.WEB.spawn(
                        world,
                        swingAnchor,
                        SpawnReason.COMMAND
                    ) ?: return@runs
                    web.owner = player
                    web.isDummy = true

                    val swingData =
                        PlayerSwingData(
                            swingAnchor = Vec3d(
                                swingAnchor.x.toDouble(),
                                swingAnchor.y.toDouble(),
                                swingAnchor.z.toDouble()
                            ),
                            startTime = System.currentTimeMillis() / 1000.0
                        )
                    playerSwingDataMap[player.uuid] = swingData
                    player.sendMessage("Started swinging.".literal)
                }
            }
        }


        ServerTickEvents.END_SERVER_TICK.register(this)
    }

    private fun performSwingMotion(player: ServerPlayerEntity) {
        val swingData = playerSwingDataMap[player.uuid] ?: return
        val isPressingForward = MinecraftClient.getInstance().player?.input?.pressingForward ?: false
        val isPressingLeft = MinecraftClient.getInstance().player?.input?.pressingLeft ?: false
        val isPressingRight = MinecraftClient.getInstance().player?.input?.pressingRight ?: false

        if (isPressingForward) {
            swingData.forwardMultiplier = Math.min(swingData.forwardMultiplier + 0.005, 5.0)
        } else {
            swingData.forwardMultiplier = Math.max(swingData.forwardMultiplier - 0.005, 1.0)
        }

        // Seitliche Bewegung
        if (isPressingLeft) {
            swingData.lateralMultiplier = Math.max(swingData.lateralMultiplier - 1, -30.0) // Limit nach links
        } else if (isPressingRight) {
            swingData.lateralMultiplier = Math.min(swingData.lateralMultiplier + 1, 30.0) // Limit nach rechts
        } else {
            // Auto-Zentrierung oder Dämpfung der seitlichen Bewegung, wenn keine seitliche Eingabe erfolgt
            swingData.lateralMultiplier *= 0.9
        }

        player.sendMessage("Forward Multiplier: ${swingData.forwardMultiplier}".literal)
        player.sendMessage("Lateral Multiplier: ${swingData.lateralMultiplier}".literal)

        // Schwerkraft
        val gravity = 9.81 * 2 //Wie stark man schwing anpassen mit W taste
        val ropeLength = 10.0
        // Startwinkel des Pendels in Grad (umgerechnet in Radiant)
        val startAngle = Math.toRadians(45.0)

        val currentTime = System.currentTimeMillis() / 1000.0 - swingData.startTime
        val angle = startAngle * cos(sqrt(gravity / ropeLength) * currentTime)


        // Berechne die zukünftige Position basierend auf dem aktuellen Zeitpunkt + deltaTime
        val futureTime = currentTime + deltaTime
        val futureAngle = startAngle * cos(sqrt(gravity / ropeLength) * futureTime)

        // Berechne aktuelle und zukünftige Positionen relativ zum Ankerpunkt
        val currentPosition = Vec3d(0.0, swingData.swingAnchor.y - (cos(angle) * ropeLength), sin(angle) * ropeLength)
        
        val futurePosition =
            Vec3d(0.0, swingData.swingAnchor.y - (cos(futureAngle) * ropeLength), sin(futureAngle) * ropeLength)

        // Berechne die erforderliche Geschwindigkeit
        var velocity = futurePosition.subtract(currentPosition).multiply(1 / deltaTime)

        // Einführung einer Aufwärmphase, um die initiale Beschleunigung zu kontrollieren
        val factor = 0.1
        val warmupTime = 5.0 // 5 Sekunden Aufwärmphase
        val dampingFactor = if (currentTime < warmupTime) {
            // Reduziere die Geschwindigkeit in der Aufwärmphase
            (currentTime / warmupTime) * factor
        } else {
            factor
        }

        velocity = velocity.multiply(dampingFactor).multiply(swingData.forwardMultiplier)
        player.modifyVelocity(velocity)
    }


    override fun onEndTick(server: MinecraftServer) {
        for (player in server.playerManager.playerList) {
            performSwingMotion(player)
        }
    }
}
