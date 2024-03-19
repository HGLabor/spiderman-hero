package gg.norisk.heroes.spiderman.entity

import gg.norisk.heroes.spiderman.movement.PullMovement
import gg.norisk.heroes.spiderman.player.gravity
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.silkmc.silk.core.entity.modifyVelocity
import java.util.*


class WebEntity : ThrownItemEntity {
    var isCollided = false
    var length = 30
    var originPos: Vec3d = pos
    var isDummy = false

    companion object {
        val OWNER =
            DataTracker.registerData(WebEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)
    }

    init {
        ignoreCameraFrustum = true
    }

    var ownerId: UUID?
        get() {
            return this.dataTracker.get(OWNER).orElse(null)
        }
        set(value) {
            this.dataTracker.set(OWNER, Optional.ofNullable(value))
        }

    // Hauptkonstruktor, der direkt den Superkonstruktor aufruft
    constructor(type: EntityType<out WebEntity>, world: World) : super(type, world)

    // Sekundärer Konstruktor, der den EntityType und World vom LivingEntity bekommt
    constructor(world: World, livingEntity: LivingEntity) : super(EntityRegistry.WEB, livingEntity, world)

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        isCollided = true
    }

    override fun initDataTracker() {
        super.initDataTracker()
        this.dataTracker.startTracking(OWNER, Optional.empty())
    }

    override fun getOwner(): Entity? {
        return this.world.getPlayerByUuid(ownerId ?: return null)
    }

    override fun setOwner(entity: Entity?) {
        super.setOwner(entity)
        this.ownerId = entity?.uuid
    }

    override fun isTouchingWater(): Boolean {
        return super.isTouchingWater()
    }

    override fun tick() {
        if (world.isClient) return
        if (isDummy) return

        if (!isCollided) {
            super.tick()
        }

        if (!world.isClient && isCollided) {
            val player = owner as? PlayerEntity? ?: return

            applySwingMotion(player, world, this.pos)

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

    fun applySwingMotion(player: PlayerEntity, world: World, anchorPoint: Vec3d) {
        val playerPosition = player.pos
        val ropeVector = anchorPoint.subtract(playerPosition)
        val ropeLength = ropeVector.length()
        val swingAngle = Math.atan2(ropeVector.y, Math.sqrt(ropeVector.x * ropeVector.x + ropeVector.z * ropeVector.z))

        // Berechne die Schwerkraftkomponenten
        val gravity = Vec3d(0.0, player.gravity.toDouble(), 0.0)
        val gravityParallel = Math.cos(swingAngle) * gravity.y
        val gravityPerpendicular = Math.sin(swingAngle) * gravity.y

        // Berechne die Zentrifugalkraft
        val playerVelocity = player.velocity
        val centrifugalForceMagnitude = (playerVelocity.lengthSquared() / ropeLength)
        val centrifugalForceDirection = ropeVector.crossProduct(Vec3d(0.0, 1.0, 0.0)).normalize()
        val centrifugalForce = centrifugalForceDirection.multiply(centrifugalForceMagnitude)

        // Neue Geschwindigkeit des Spielers berechnen
        val newVelocity =
            playerVelocity.add(Vec3d(0.0, gravityParallel + gravityPerpendicular, 0.0)).add(centrifugalForce)

        // Aktualisiere die Spieler-Geschwindigkeit
        player.modifyVelocity(newVelocity)
    }
}
