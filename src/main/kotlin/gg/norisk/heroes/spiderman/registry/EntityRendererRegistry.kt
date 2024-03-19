package gg.norisk.heroes.spiderman.registry

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.FlyingItemEntityRenderer

object EntityRendererRegistry {
    fun init() {
        EntityRendererRegistry.register(EntityRegistry.WEB, ::FlyingItemEntityRenderer)
    }
}
