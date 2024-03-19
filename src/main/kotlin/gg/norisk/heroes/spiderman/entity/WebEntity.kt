package gg.norisk.heroes.spiderman.entity

import gg.norisk.heroes.spiderman.movement.PullMovement
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.silkmc.silk.core.entity.modifyVelocity


class WebEntity : ThrownItemEntity {
    var isCollided = false
    var length = 30
    var originPos: Vec3d = pos

    // Hauptkonstruktor, der direkt den Superkonstruktor aufruft
    constructor(type: EntityType<out WebEntity>, world: World) : super(type, world)

    // Sekundärer Konstruktor, der den EntityType und World vom LivingEntity bekommt
    constructor(world: World, livingEntity: LivingEntity) : super(EntityRegistry.WEB, livingEntity, world)

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        isCollided = true
    }

    override fun isTouchingWater(): Boolean {
        return super.isTouchingWater()
    }

    override fun tick() {
        if (!isCollided) {
            super.tick()
        }

        if (!world.isClient && isCollided) {
            val player = owner as? PlayerEntity? ?: return
            if (PullMovement.isPulling.contains(player.uuid)) {
                val direction = this.pos.subtract(player.pos).normalize()
                //Kann man noch anpassen
                val speed = if (player.distanceTo(this) < 2) {
                    0.2
                } else {
                    1.5
                }
                player.modifyVelocity(direction.multiply(speed))
            }
        }

        if (this.pos.distanceTo(originPos) >= length && !world.isClient) {
            this.modifyVelocity(this.velocity.multiply(0.8))
        }
    }

    override fun getGravity(): Float {
        if (this.pos.distanceTo(originPos) >= length && !world.isClient) {
            return super.getGravity() * 2
        }
        return super.getGravity()
    }

    override fun getDefaultItem(): Item {
        return Items.COBWEB
    }
}
