package gg.norisk.heroes.spiderman.movement

import kotlinx.coroutines.cancel
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.commands.player
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import net.silkmc.silk.core.text.literal
import java.awt.Point
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object Parabel : ClientTickEvents.EndTick {
    data class Parabola(
        val origin: Vec3d,
        val direction: Vec3d,
        val a: Double,
        val b: Double,
        val c: Double,
        val length: Double,
        val particleCount: Int,
        val rollAngle: Double // Rollwinkel in Grad
    )

    data class PlayerSwingData(
        val parabola: Parabola,
        val startTime: Double
    )

    val swingDatas = mutableMapOf<UUID, PlayerSwingData>()

    fun init() {
        clientCommand("parabel") {
            literal("visualize") {
                argument<Double>("a") { a ->
                    runs {
                        val player = this.source.player
                        visualizeParabola(
                            player,
                            Parabola(
                                origin = player.pos,
                                direction = player.rotationVector,
                                a(),
                                0.0,
                                player.y,
                                30.0,
                                200,
                                0.0
                            )
                        )
                    }
                }
                runs {
                    val player = this.source.player
                    val origin = player.pos

                    val endTime = System.currentTimeMillis() + 5000L
                    val parabel =
                        Parabola(
                            origin = origin,
                            direction = player.rotationVector,
                            0.05,
                            0.0,
                            player.y,
                            30.0,
                            50,
                            0.0
                        )

                    swingDatas[player.uuid] = PlayerSwingData(
                        parabola = parabel,
                        startTime = System.currentTimeMillis() / 1000.0
                    )
                }
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register(this)
    }

    private fun movePlayerAlongParabola(player: ClientPlayerEntity, movementData: PlayerSwingData) {
        val world = player.world
        val currentTime = System.currentTimeMillis() / 1000.0 - movementData.startTime
        val totalDuration = movementData.parabola.length / 10.0
        val fraction = (currentTime % totalDuration) / totalDuration
        val x = movementData.parabola.length * fraction - movementData.parabola.length / 2
        val y = movementData.parabola.a * x * x + movementData.parabola.b * x

        // Berechnung der neuen Position unter Berücksichtigung der Roll-Achse
        val rollRadians = Math.toRadians(movementData.parabola.rollAngle)
        val rotatedX = x * Math.cos(rollRadians) - y * Math.sin(rollRadians)
        val rotatedY = x * Math.sin(rollRadians) + y * Math.cos(rollRadians)

        val direction = Vec3d(movementData.parabola.direction.x, 0.0, movementData.parabola.direction.z).normalize()
        val offset = direction.multiply(rotatedX).add(0.0, rotatedY, 0.0)

        val newPos = Vec3d(
            movementData.parabola.origin.x + offset.x,
            movementData.parabola.origin.y + offset.y,
            movementData.parabola.origin.z + offset.z
        )

        if (world.isClient) {
            player.setPosition(newPos.x, newPos.y, newPos.z)
        }
    }

    private fun visualizeParabola(player: ClientPlayerEntity, parabola: Parabola) {
        val world = player.world
        val direction = Vec3d(parabola.direction.x, 0.0, parabola.direction.z).normalize()

        for (i in 0 until parabola.particleCount) {
            val fraction = i.toDouble() / (parabola.particleCount - 1)
            val x = (parabola.length * fraction) - (parabola.length / 2)
            val y = parabola.a * x * x + parabola.b * x + parabola.c

            // Calculate the offset position based on the direction the player is looking
            val offset = direction.multiply(x).add(0.0, y, 0.0)
            val pos = Vec3d(
                parabola.origin.x + offset.x,
                offset.y,
                parabola.origin.z + offset.z
            )

            world.addParticle(ParticleTypes.HEART, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0)
        }
    }

    override fun onEndTick(client: MinecraftClient) {
        val swingData = swingDatas[client.player?.uuid] ?: return
        visualizeParabola(
            client.player!!,
            swingData.parabola
        )
    }
}
