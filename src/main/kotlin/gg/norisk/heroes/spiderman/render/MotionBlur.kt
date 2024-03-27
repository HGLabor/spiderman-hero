package gg.norisk.heroes.spiderman.render

import gg.norisk.heroes.spiderman.Manager.logger
import gg.norisk.heroes.spiderman.Manager.toId
import gg.norisk.heroes.spiderman.mixin.client.gl.PostEffectProcessorAccessor
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.PostEffectProcessor
import net.minecraft.client.gui.DrawContext

object MotionBlur {
    private var lastTimestampInGame: Long = 0

    private val strength = 70
    private val shaderLocation = "shaders/post/motion_blur.json".toId()
    private var shader: PostEffectProcessor? = null
    private var currentBlur = 0f
    private var lastWidth = 0
    private var lastHeight = 0


    fun initClient() {
        val mc = MinecraftClient.getInstance()

        HudRenderCallback.EVENT.register(HudRenderCallback { context: DrawContext, delta: Float ->
            if ((shader == null || mc.window.width != lastWidth || mc.window.height != lastHeight) && mc.window.width > 0 && mc.window.height > 0) {
                currentBlur = accumValue
                runCatching {
                    shader = PostEffectProcessor(mc.textureManager, mc.resourceManager, mc.framebuffer, shaderLocation)
                    shader!!.setupDimensions(mc.window.width, mc.window.height)
                }.onFailure {
                    logger.error("Failed to load shader", it)
                }
            }
            if (currentBlur != accumValue && shader != null) {
                (shader as PostEffectProcessorAccessor).passes.forEach { shader ->
                    shader.program.getUniformByName("BlendFactor")?.set(accumValue)
                }
                currentBlur = accumValue
            }

            lastWidth = mc.window.width
            lastHeight = mc.window.height

            shader?.render(delta)
        })
    }

    private val accumValue: Float
        get() {
            if (MinecraftClient.getInstance().world == null) {
                return 0.0f
            }
            var percent = multiplier * 10.0f
            if (MinecraftClient.getInstance().currentScreen == null) {
                lastTimestampInGame = System.currentTimeMillis()
                if (percent > 996.0f) {
                    percent = 996.0f
                }
            } else if (percent > 990.0f) {
                percent = 990.0f
            }
            val fadeOut = System.currentTimeMillis() - lastTimestampInGame
            if (fadeOut > 10000L) {
                return 0.0f
            }
            if (percent < 0.0f) {
                percent = 0.0f
            }
            return percent / 1000.0f
        }

    private val multiplier: Float
        get() {
            val fps = (MinecraftClient.getInstance().currentFps)
            val multiplier = strength.toFloat()
            return when {
                fps > 200 -> multiplier
                fps > 59 -> multiplier * 0.5f
                else -> 0f
            }
        }
}
