package gg.norisk.heroes.spiderman.render

import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.player.isSwinging
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.BatEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Arm
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.LightType
import net.silkmc.silk.core.text.literal
import org.joml.Matrix4f
import kotlin.math.cos
import kotlin.math.sin

object LeadRenderer {

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
        val vec3d =
            if (player == MinecraftClient.getInstance().player && MinecraftClient.getInstance().options.perspective.isFirstPerson) {
                val g: Float = MathHelper.lerp(f * 0.5f, player.yaw, player.prevYaw) * (Math.PI / 180.0).toFloat()
                val h: Float = MathHelper.lerp(f * 0.5f, player.pitch, player.prevPitch) * (Math.PI / 180.0).toFloat()
                val d = if (player.mainArm == Arm.RIGHT) -1.0 else 1.0
                val vec3d = Vec3d(0.39 * d, 0.0, 0.3)
                vec3d.rotateX(-h).rotateY(-g).add(player.getCameraPosVec(f))
            } else {
                if (player.isSwinging) {
                    val h: Float = MathHelper.lerp(f, player.prevBodyYaw, player.bodyYaw) * (Math.PI / 180.0).toFloat()
                    val m: Double = player.boundingBox.lengthY + 0.2
                    val e = if (player.isInSneakingPose) -0.2 else 0.07
                    player.getLerpedPos(f).add(Vec3d(0.0, m, e).rotateY(-h))
                } else {
                    val d = 0.22 * (if (player.mainArm == Arm.RIGHT) -1.0 else 1.0)
                    val g: Float =
                        MathHelper.lerp(f * 0.5f, player.pitch, player.prevPitch) * (Math.PI / 180.0).toFloat()
                    val h: Float = MathHelper.lerp(f, player.prevBodyYaw, player.bodyYaw) * (Math.PI / 180.0).toFloat()
                    val m: Double = player.boundingBox.lengthY - 1.0
                    val e = if (player.isInSneakingPose) -0.2 else 0.07
                    player.getLerpedPos(f).add(Vec3d(d, m, e).rotateY(-h))
                }
            }
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
