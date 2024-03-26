package gg.norisk.heroes.spiderman.render

import com.mojang.blaze3d.systems.RenderSystem
import gg.norisk.heroes.spiderman.Manager.toId
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.GlUniform
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.MathHelper
import kotlin.math.max
import kotlin.math.min


object Speedlines {
    var client: MinecraftClient = MinecraftClient.getInstance()
    var lerpedSpeed: Double = 0.0

    lateinit var edge: GlUniform
    lateinit var speedlinesRenderTypeProgram: ShaderProgram
    
    fun initClient() {
        CoreShaderRegistrationCallback.EVENT.register(CoreShaderRegistrationCallback { context: CoreShaderRegistrationCallback.RegistrationContext ->
            context.register(
                "speedlines".toId(), VertexFormats.POSITION
            ) { shaderProgram: ShaderProgram ->
                speedlinesRenderTypeProgram = shaderProgram
                edge = shaderProgram.getUniform("Edge")!!
            }
        })

        HudRenderCallback.EVENT.register(HudRenderCallback { context: DrawContext, delta: Float ->
            val width = client.getWindow().width.toFloat()
            val height = client.getWindow().height.toFloat()

            lerpedSpeed = MathHelper.lerp((delta * 0.05f).toDouble(), lerpedSpeed, client.player!!.velocity.length())

            var speed = max(0.0, (lerpedSpeed - 0.2) / 2f)
            speed = min(speed, 0.2)
            edge.set((0.5f - speed).toFloat())

            val positionMatrix = context.matrices.peek().positionMatrix
            val tessellator = Tessellator.getInstance()
            val buffer1 = tessellator.buffer
            buffer1.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
            buffer1.vertex(positionMatrix, 0f, height, 0f).next()
            buffer1.vertex(positionMatrix, 0f, 0f, 0f).next()
            buffer1.vertex(positionMatrix, width, 0f, 0f).next()
            buffer1.vertex(positionMatrix, width, height, 0f).next()
            RenderSystem.setShader { speedlinesRenderTypeProgram }
            setupRender()
            BufferRenderer.drawWithGlobalProgram(buffer1.end())
            endRender()
        })
    }

    private fun setupRender() {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    private fun endRender() {
        RenderSystem.disableBlend()
    }
}
