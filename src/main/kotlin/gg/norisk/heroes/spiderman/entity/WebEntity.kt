package gg.norisk.heroes.spiderman.entity

import gg.norisk.heroes.spiderman.grapple.GrappleModUtils
import gg.norisk.heroes.spiderman.grapple.GrapplingHookPhysicsController
import gg.norisk.heroes.spiderman.grapple.RopeSegmentHandler
import gg.norisk.heroes.spiderman.movement.PullMovement
import gg.norisk.heroes.spiderman.player.gravity
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import gg.norisk.heroes.spiderman.util.Vec
import net.minecraft.client.MinecraftClient
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
import net.silkmc.silk.core.text.literal
import java.util.*


class WebEntity : ThrownItemEntity {
    var originPos: Vec3d = pos
    var isDummy = false
    var ropeLength: Double = 30.0
    var taut: Double = 1.0
    val segmentHandler: RopeSegmentHandler = RopeSegmentHandler(
        this,
        Vec.positionVec(this),
        Vec.positionVec(this)
    )

    companion object {
        val OWNER =
            DataTracker.registerData(WebEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)
        val COLLIDED =
            DataTracker.registerData(WebEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
    }

    init {
        ignoreCameraFrustum = true
    }

    var isCollided: Boolean
        get() {
            return this.dataTracker.get(COLLIDED)
        }
        set(value) {
            this.dataTracker.set(COLLIDED, value)
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
        this.dataTracker.startTracking(COLLIDED, false)
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
        val player = owner as? PlayerEntity? ?: return

        if (world.isClient) {
            player.sendMessage("RopeLength: $ropeLength".literal)
        }

        //handleHookPhysics()

        if (world.isClient && isCollided) {
            if (owner == MinecraftClient.getInstance().player && GrappleModUtils.controller == null) {
                owner?.sendMessage("Creating Controller".literal)
                GrappleModUtils.controller = GrapplingHookPhysicsController(this.id, owner!!.id, this.world)
            }

            if (!this.isAlive) {
                GrappleModUtils.controller = null
            }
        }

        if (!isCollided) {
            super.tick()
        }

        if (this.pos.distanceTo(originPos) >= ropeLength && !world.isClient) {
            this.modifyVelocity(this.velocity.multiply(0.8))
        }

        if (world.isClient) return
        if (isDummy) return

        if (!world.isClient && isCollided) {

            /*applySwingMotion(player, world, this.pos)

            if (PullMovement.isPulling.contains(player.uuid)) {
                val direction = this.pos.subtract(player.pos).normalize()
                //Kann man noch anpassen
                val speed = if (player.distanceTo(this) < 2) {
                    0.2
                } else {
                    1.5
                }
                player.modifyVelocity(direction.multiply(speed))
            }*/
        }
    }

    override fun getGravity(): Float {
        if (this.pos.distanceTo(originPos) >= ropeLength) {
            return super.getGravity() * 2
        }
        return super.getGravity()
    }

    override fun getDefaultItem(): Item {
        return Items.COBWEB
    }

    private fun handleHookPhysics() {
        if (segmentHandler.hookPastBend(this.ropeLength)) {
            val farthest = segmentHandler.farthest
            //this.serverAttach(segmentHandler.getBendBlock(1), farthest, null)
        }

        //if (!this.customization.get(BLOCK_PHASE_ROPE.get())) {
        if (!false) {
            segmentHandler.update(
                Vec.positionVec(this), Vec.positionVec(this.owner).add(
                    Vec(
                        0.0,
                        this.owner!!.standingEyeHeight.toDouble(), 0.0
                    )
                ), this.ropeLength, true
            )

            /* if (this.customization.get(STICKY_ROPE.get())) {
                 val segments = segmentHandler.getSegments()

                 if (segments.size > 2) {
                     val bendnumber = segments.size - 2
                     val closest = segments[bendnumber]
                     val blockpos: BlockPos = segmentHandler.getBendBlock(bendnumber)

                     for (i in 1..bendnumber) segmentHandler.removeSegment(1)

                     this.serverAttach(blockpos, closest, null)
                 }
             }*/
        } /*else {
            segmentHandler.updatePos(
                Vec.positionVec(this), Vec.positionVec(this.shootingEntity).add(
                    Vec(
                        0,
                        this.shootingEntity.getEyeHeight(), 0
                    )
                ), this.ropeLength
            )
        }*/

        val farthest = segmentHandler.farthest
        val distToFarthest = segmentHandler.distToFarthest

        val ropevec = Vec.positionVec(this).sub(farthest)
        val d = ropevec.length()

        /*if (this.customization.get(HOOK_REEL_IN_ON_SNEAK.get()) && this.owner?.isSneaking == true) {
            val newdist = d + distToFarthest - 0.4
            if (newdist > 1 && newdist <= this.customization.get(MAX_ROPE_LENGTH.get())) {
                this.ropeLength = newdist
            }
        }*/


        /*if (d + distToFarthest > this.ropeLength) {
            var motion = Vec.motionVec(this)

            if (motion.dot(ropevec) > 0) {
                motion = motion.removeAlong(ropevec)
            }

            //this.setVelocityActually(motion.x, motion.y, motion.z)
            this.modifyVelocity(motion.x, motion.y, motion.z)

            ropevec.mutableSetMagnitude(this.ropeLength - distToFarthest)
            val newpos = ropevec.add(farthest)

            this.setPos(newpos.x, newpos.y, newpos.z)
        }*/
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
