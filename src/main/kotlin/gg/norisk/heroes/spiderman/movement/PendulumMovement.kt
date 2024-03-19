package gg.norisk.heroes.spiderman.movement

import gg.norisk.heroes.spiderman.registry.EntityRegistry
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.SpawnReason
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
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
                    val swingAnchor = player.blockPos.add(0, 10, 0)
                    val world = player.serverWorld

                    /*player.world.setBlockState(
                        BlockPos(
                            swingAnchor.x,
                            swingAnchor.y,
                            swingAnchor.z
                        ),
                        Blocks.BEDROCK.defaultState
                    )*/

                    val web = EntityRegistry.WEB.spawn(
                        world,
                        swingAnchor,
                        SpawnReason.COMMAND
                    ) ?: return@runs
                    web.setNoGravity(false)
                    web.customName = "Web".literal
                    web.isCustomNameVisible = true

                    val swingData =
                        PlayerSwingData(swingAnchor = web.pos, startTime = System.currentTimeMillis() / 1000.0)
                    playerSwingDataMap[player.uuid] = swingData
                    player.sendMessage("Started swinging.".literal)
                }
            }
        }


        ServerTickEvents.END_SERVER_TICK.register(this)
    }

    private fun performSwingMotion(player: ServerPlayerEntity) {
        val swingData = playerSwingDataMap[player.uuid] ?: return
        val currentPosition = player.pos
        val currentDistanceVec = currentPosition.subtract(swingData.swingAnchor)
        val currentDistance = currentDistanceVec.length()

        // Berechne, wie weit der Spieler über die "Seillänge" hinaus ist
        val overLength = currentDistance - ropeLength

        // Stärkere Rückführkraft, wenn der Spieler über die Seillänge hinaus ist
        val kBase = 0.05 // Grundfederkonstante
        val kOverLengthMultiplier = 3.0 // Multiplikator für die Federkonstante über die Seillänge hinaus
        val k = if (overLength > 0) kBase * kOverLengthMultiplier else kBase

        // Die "Federkraft", die auf den Spieler ausgeübt wird, abhängig vom Abstand zum Ankerpunkt
        val forceMagnitude = overLength * k

        // Berechne die Richtung der Kraft
        val forceDirection = currentDistanceVec.normalize().negate()

        // Berechne die neue Geschwindigkeit basierend auf der "Federkraft"
        var newVelocity = forceDirection.multiply(forceMagnitude)

        // Einfache Dämpfung, um die Bewegung realistischer zu machen
        val dampingFactor = 0.1
        newVelocity = newVelocity.multiply(dampingFactor)

        // Anwenden der berechneten Geschwindigkeit
        player.modifyVelocity(newVelocity)
    }


    override fun onEndTick(server: MinecraftServer) {
        for (player in server.playerManager.playerList) {
            performSwingMotion(player)
        }
    }
}
