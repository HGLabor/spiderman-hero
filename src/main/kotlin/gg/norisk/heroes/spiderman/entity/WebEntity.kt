package gg.norisk.heroes.spiderman.entity

import gg.norisk.heroes.spiderman.registry.EntityRegistry
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World
import net.silkmc.silk.core.text.broadcastText

class WebEntity : ThrownItemEntity {
    var isCollided = false
    // Hauptkonstruktor, der direkt den Superkonstruktor aufruft
    constructor(type: EntityType<out WebEntity>, world: World) : super(type, world)

    // Sekundärer Konstruktor, der den EntityType und World vom LivingEntity bekommt
    constructor(world: World, livingEntity: LivingEntity) : super(EntityRegistry.WEB, livingEntity, world) {
        // Zusätzliche Initialisierung, falls nötig
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        isCollided = true
    }

    override fun tick() {
        if (!isCollided) {
            super.tick()
        }
    }

    override fun getDefaultItem(): Item {
        return Items.COBWEB
    }
}
