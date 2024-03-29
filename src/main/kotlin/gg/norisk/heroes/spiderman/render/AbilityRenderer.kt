package gg.norisk.heroes.spiderman.render

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import net.silkmc.silk.core.world.pos.Pos2i
import java.awt.Color

data class Ability(
    val keybinding: KeyBinding,
    val shouldRender: ((AbstractClientPlayerEntity) -> Boolean) = { true },
    val description: ((AbstractClientPlayerEntity) -> String),
    val keyText: String? = null,
    val prefix: Text? = null
)

object AbilityRenderer : HudRenderCallback {
    val abilities = mutableSetOf<Ability>()

    fun init() {
        HudRenderCallback.EVENT.register(this)
    }

    override fun onHudRender(drawContext: DrawContext, tickDelta: Float) {
        if (MinecraftClient.getInstance().options.hudHidden) return
        val player = MinecraftClient.getInstance().player ?: return
        val offset = 2
        drawContext.matrices.push()
        val scale = 0.8f
        drawContext.matrices.scale(scale, scale, scale)
        abilities.filter { it.shouldRender(player) }.forEachIndexed { index, ability ->
            val text = literalText {
                /*if (ability.hold) {
                    text("Hold ") { color = 0x47CD45 }
                }*/
                if (ability.prefix != null) {
                    text(ability.prefix)
                }
                text(if (ability.keyText != null) ability.keyText.literal else ability.keybinding.boundKeyLocalizedText) {
                    if (ability.keybinding.isPressed) {
                        color = Color.decode("#cc0000").brighter().brighter().rgb
                    } else {
                        color = 0xcc0000
                    }
                }
                text(" - ") { color = 0x919191 }
                text(ability.description.invoke(player))
            }
            val pos = Pos2i(5, 5 + (text.height + offset * 2) * index)
            drawContext.fill(
                RenderLayer.getGuiOverlay(),
                pos.x - offset,
                pos.z - offset,
                pos.x + text.width + offset,
                pos.z + text.height + offset,
                -1873784752
            )
            drawContext.drawText(
                MinecraftClient.getInstance().textRenderer,
                text,
                pos.x,
                pos.z,
                14737632,
                true
            )
        }
        drawContext.matrices.pop()
    }

    val Text.width
        get() = MinecraftClient.getInstance().textRenderer.getWidth(this)

    val Text.height
        get() = MinecraftClient.getInstance().textRenderer.fontHeight
}
