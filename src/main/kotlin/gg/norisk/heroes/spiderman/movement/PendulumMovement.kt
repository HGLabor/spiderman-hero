package gg.norisk.heroes.spiderman.movement

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Blocks
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.text.literal
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PendulumMovement : ServerTickEvents.EndTick {
    data class PlayerSwingData(
        var swingAnchor: Vec3d,
        var startTime: Double = 0.0
    )

    val playerSwingDataMap = mutableMapOf<UUID, PlayerSwingData>()

    // Der Ankerpunkt für das Schwingen

    // Die Länge des Seils
    private val ropeLength = 30.0

    // Schwerkraft
    private val gravity = 9.81

    // Startwinkel des Pendels in Grad (umgerechnet in Radiant)
    private val startAngle = Math.toRadians(45.0)

    // Zeit in Sekunden seit Start des Schwingens

    // Zeit-Update-Rate (Annahme: Server Tick Rate = 20 Ticks pro Sekunde)
    private val deltaTime = 1.0 / 20.0

    fun initialize() {
        command("swing") {
            runs {
                val player = this.source.playerOrThrow
                if (playerSwingDataMap.containsKey(player.uuid)) {
                    playerSwingDataMap.remove(player.uuid)
                    player.sendMessage("Stopped swinging.".literal)
                } else {
                    // Setze einen neuen Ankerpunkt und Startzeit für den Spieler
                    val swingAnchor = player.pos.add(0.0, 50.0, 0.0)

                    player.world.setBlockState(
                        BlockPos(
                            swingAnchor.x.toInt(),
                            swingAnchor.y.toInt(),
                            swingAnchor.z.toInt()
                        ),
                        Blocks.BEDROCK.defaultState
                    )

                    val swingData =
                        PlayerSwingData(swingAnchor = swingAnchor, startTime = System.currentTimeMillis() / 1000.0)
                    playerSwingDataMap[player.uuid] = swingData
                    player.sendMessage("Started swinging.".literal)
                }
            }
        }


        ServerTickEvents.END_SERVER_TICK.register(this)
    }

    private fun performSwingMotion(player: ServerPlayerEntity) {
        val swingData = playerSwingDataMap[player.uuid] ?: return
        val currentTime = System.currentTimeMillis() / 1000.0 - swingData.startTime
        val angle = startAngle * cos(sqrt(gravity / ropeLength) * currentTime)

        // Berechne die zukünftige Position basierend auf dem aktuellen Zeitpunkt + deltaTime
        val futureTime = currentTime + deltaTime
        val futureAngle = startAngle * cos(sqrt(gravity / ropeLength) * futureTime)

        // Berechne aktuelle und zukünftige Positionen relativ zum Ankerpunkt
        val currentPosition = Vec3d(sin(angle) * ropeLength, swingData.swingAnchor.y - (cos(angle) * ropeLength), 0.0)
        val futurePosition = Vec3d(sin(futureAngle) * ropeLength, swingData.swingAnchor.y - (cos(futureAngle) * ropeLength), 0.0)

        // Berechne die erforderliche Geschwindigkeit
        var velocity = futurePosition.subtract(currentPosition).multiply(1 / deltaTime)

        // Einführung einer Aufwärmphase, um die initiale Beschleunigung zu kontrollieren
        val warmupTime = 5.0 // 5 Sekunden Aufwärmphase
        val dampingFactor = if (currentTime < warmupTime) {
            // Reduziere die Geschwindigkeit in der Aufwärmphase
            (currentTime / warmupTime) * 0.5
        } else {
            0.5
        }

        velocity = velocity.multiply(dampingFactor)
        player.modifyVelocity(velocity)
    }


    override fun onEndTick(server: MinecraftServer) {
        for (player in server.playerManager.playerList) {
            performSwingMotion(player)
        }
    }
}
