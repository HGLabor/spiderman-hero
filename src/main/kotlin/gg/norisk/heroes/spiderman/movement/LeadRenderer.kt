package gg.norisk.heroes.spiderman.movement

import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.player.setLeashTarget
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.BatEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.LightType
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.text.literal
import org.joml.Matrix4f
import kotlin.math.cos
import kotlin.math.sin

object LeadRenderer {
    fun init() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            command("leadtest") {
                runs {
                    val player = this.source.playerOrThrow
                    val world = player.serverWorld
                    val web = EntityType.BAT.spawn(world, player.blockPos.add(0, 5, 0), SpawnReason.COMMAND)
                        ?: return@runs
                    web.attachLeash(player, true)
                    web.setNoGravity(false)
                    web.customName = "Web".literal
                    web.isCustomNameVisible = true
                    web.isAiDisabled = true


                    val bat = EntityType.BAT.spawn(world, player.blockPos.add(0, 5, 3), SpawnReason.COMMAND)
                        ?: return@runs
                    bat.attachLeash(player, true)
                    bat.isAiDisabled = true
                    bat.setNoGravity(false)
                    bat.customName = "Bat".literal
                    bat.isCustomNameVisible = true
                }
            }
            command("leadtest2") {
                runs {
                    val player = this.source.playerOrThrow
                    val world = player.serverWorld
                    val bat = EntityType.BAT.spawn(world, player.blockPos.add(0, 5, 3), SpawnReason.COMMAND)
                        ?: return@runs
                    player.setLeashTarget(bat)
                }
            }
        }
    }

    fun <T : MobEntity, E : Entity> checkForSpidermanLeash(mobEntity: T, entity: E): Boolean {
        if (entity is PlayerEntity && mobEntity is BatEntity && mobEntity.customName == "Bat".literal) {
            return true
        } else {
            return false
        }
    }

    //TODO die welt ist irgendwie fucked
    fun renderLeash(
        f: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        mobEntity: WebEntity,
        player: PlayerEntity
    ) {
        matrixStack.push()
        val vec3d = player.getLeashPos(f).add(0.0,0.5,0.0)
        val d: Double =
            (MathHelper.lerp(
                f,
                mobEntity.bodyYaw,
                mobEntity.bodyYaw
            ) * (Math.PI / 180.0).toFloat()).toDouble() + (Math.PI / 2)
        val vec3d2: Vec3d = mobEntity.getLeashOffset(f)
        val e = cos(d) * vec3d2.z + sin(d) * vec3d2.x
        val g = sin(d) * vec3d2.z - cos(d) * vec3d2.x
        val h = MathHelper.lerp(f.toDouble(), mobEntity.prevX, mobEntity.x) + e
        val i = MathHelper.lerp(f.toDouble(), mobEntity.prevY, mobEntity.y) + vec3d2.y
        val j = MathHelper.lerp(f.toDouble(), mobEntity.prevZ, mobEntity.z) + g
        matrixStack.translate(e, vec3d2.y, g)
        val k = (vec3d.x - h).toFloat()
        val l = (vec3d.y - i).toFloat()
        val m = (vec3d.z - j).toFloat()
        val n = 0.025f
        val vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLeash())
        val matrix4f = matrixStack.peek().positionMatrix
        val o = MathHelper.inverseSqrt(k * k + m * m) * 0.025f / 2.0f
        val p = m * o
        val q = k * o
        val blockPos = BlockPos.ofFloored(mobEntity.getCameraPosVec(f))
        val blockPos2 = BlockPos.ofFloored(player.getCameraPosVec(f))
        val r: Int = 15
        val s: Int = 15
        val t: Int = mobEntity.getWorld().getLightLevel(LightType.SKY, blockPos)
        val u: Int = mobEntity.getWorld().getLightLevel(LightType.SKY, blockPos2)

        for (v in 0..24) {
            renderSpiderLeash(
                vertexConsumer,
                matrix4f,
                k,
                l,
                m,
                r,
                s,
                t,
                u,
                0.025f,
                0.025f,
                p,
                q,
                v,
                false
            )
        }

        for (v in 24 downTo 0) {
            renderSpiderLeash(
                vertexConsumer,
                matrix4f,
                k,
                l,
                m,
                r,
                s,
                t,
                u,
                0.025f,
                0.0f,
                p,
                q,
                v,
                true
            )
        }

        matrixStack.pop()
    }


    private fun renderSpiderLeash(
        vertexConsumer: VertexConsumer,
        matrix4f: Matrix4f,
        f: Float,
        g: Float,
        h: Float,
        i: Int,
        j: Int,
        k: Int,
        l: Int,
        m: Float,
        n: Float,
        o: Float,
        p: Float,
        q: Int,
        bl: Boolean
    ) {
        val r = q.toFloat() / 24.0f
        val s = MathHelper.lerp(r, i.toFloat(), j.toFloat()).toInt()
        val t = MathHelper.lerp(r, k.toFloat(), l.toFloat()).toInt()
        val u = LightmapTextureManager.pack(s, t)
        val v = if (q % 2 == (if (bl) 1 else 0)) 0.7f else 1.0f
        val w = 1f * v
        val x = 1f * v
        val y = 1f * v
        val z = f * r
        val aa = if (g > 0.0f) g * r * r else g - g * (1.0f - r) * (1.0f - r)
        val ab = h * r
        vertexConsumer.vertex(matrix4f, z - o, aa + n, ab + p).color(w, x, y, 1.0f).light(u).next()
        vertexConsumer.vertex(matrix4f, z + o, aa + m - n, ab - p).color(w, x, y, 1.0f).light(u).next()
    }
}
