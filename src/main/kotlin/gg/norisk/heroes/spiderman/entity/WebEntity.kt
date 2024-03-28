package gg.norisk.heroes.spiderman.entity

import gg.norisk.heroes.spiderman.Manager
import gg.norisk.heroes.spiderman.grapple.GrappleModUtils
import gg.norisk.heroes.spiderman.grapple.GrappleModUtils.webEntityDiscardPacket
import gg.norisk.heroes.spiderman.grapple.GrapplingHookPhysicsController
import gg.norisk.heroes.spiderman.grapple.RopeSegmentHandler
import gg.norisk.heroes.spiderman.player.gravity
import gg.norisk.heroes.spiderman.player.isSwinging
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import gg.norisk.heroes.spiderman.sound.FlyingSoundInstance
import gg.norisk.heroes.spiderman.util.Vec
import kotlinx.coroutines.Job
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.*
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.math.geometry.filledSpherePositionSet
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.broadcastText
import java.util.*
import kotlin.random.Random

//TODO animation / transformation und spidy sense

class WebEntity : ThrownItemEntity {
    var originPos: Vec3d = pos
    var isDummy = false
    var ropeLength: Double = 60.0
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
        val SCALE =
            DataTracker.registerData(WebEntity::class.java, TrackedDataHandlerRegistry.FLOAT)

        private val cobwebCleanUp = mutableMapOf<BlockPos, Job>()

        fun initServer() {
            webEntityDiscardPacket.receiveOnServer { packet, context ->
                for (world in context.server.worlds) {
                    val entity = world.getEntityById(packet) as? WebEntity ?: continue
                    if (entity.ownerId == context.player.uuid) {
                        entity.discard()
                    }
                }
            }
        }

        fun ServerPlayerEntity.getWeb(): WebEntity? {
            return serverWorld.iterateEntities()
                .filterIsInstance<WebEntity>().firstOrNull { it.ownerId == this.uuid }
        }

        fun removeWebs(player: PlayerEntity) {
            for (world in player.server?.worlds ?: return) {
                for (webEntity in world.iterateEntities()
                    .filterIsInstance<WebEntity>()
                    .filter { it.ownerId == player.uuid }) {
                    if (webEntity.ownerId == player.uuid) {
                        webEntity.discard()
                    }
                }
            }
        }

        fun jumpEntity(player: Entity) {
            player.modifyVelocity(player.rotationVector.normalize().multiply(5.0))
        }
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

    var scale: Float
        get() {
            return this.dataTracker.get(SCALE)
        }
        set(value) {
            this.dataTracker.set(SCALE, MathHelper.clamp(value, 0.1f, 10f))
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
        if (hitResult is EntityHitResult) {
        } else {
            isCollided = true
        }
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        val entity = entityHitResult.entity
        this.startRiding(entity, true)
    }

    override fun initDataTracker() {
        super.initDataTracker()
        this.dataTracker.startTracking(OWNER, Optional.empty())
        this.dataTracker.startTracking(COLLIDED, false)
        this.dataTracker.startTracking(SCALE, 1f)
    }

    override fun onRemoved() {
        super.onRemoved()
        (owner as? PlayerEntity?)?.isSwinging = false
    }

    override fun getLeashOffset(): Vec3d {
        return Vec3d(0.0, 0.25 * standingEyeHeight.toDouble(), 0.0)
    }

    override fun getDimensions(entityPose: EntityPose): EntityDimensions {
        return super.getDimensions(entityPose).scaled(1.0f + 1.5f * scale)
    }

    override fun onTrackedDataSet(trackedData: TrackedData<*>?) {
        super.onTrackedDataSet(trackedData)
        if (COLLIDED.equals(trackedData)) {
            if (isCollided) {
                (owner as? PlayerEntity?)?.isSwinging = true
                //pullTowardsWeb(owner)
                world.playSound(
                    owner as? PlayerEntity?,
                    this.x,
                    this.y,
                    this.z,
                    SoundEvents.ENTITY_SLIME_SQUISH,
                    SoundCategory.NEUTRAL,
                    0.5f,
                    Random.nextDouble(1.5, 2.0).toFloat()
                )
            }
        } else if (SCALE.equals(trackedData)) {
            calculateDimensions()
        }
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

    fun convertToCobwebs() {
        val vec3i = Vec3i(this.blockX, this.blockY, this.blockZ)
        for (blockPos in vec3i.filledSpherePositionSet((1 + scale / 4).toInt())) {
            if (Random.nextBoolean()) {
                val blockState = world.getBlockState(blockPos)
                if (blockState.isAir) {
                    world.setBlockState(blockPos, Blocks.COBWEB.defaultState)

                    //CLEANUP
                    cobwebCleanUp[blockPos]?.cancel()
                    cobwebCleanUp[blockPos] = mcCoroutineTask(delay = Random.nextInt(20, 300).ticks) {
                        if (world.getBlockState(blockPos).isOf(Blocks.COBWEB)) {
                            world.breakBlock(blockPos, false, owner)
                        }
                        cobwebCleanUp.remove(blockPos)
                    }
                }
            }
        }
        discard()
    }

    override fun tick() {
        //println("$world $id $uuid")
        val player = owner as? PlayerEntity? ?: return

        if (player.isDead) {
            discard()
        }

        if (world.isClient && !this.isAlive) {
            //player.sendMessage("RopeLength: $ropeLength".literal)
        }

        //handleHookPhysics()

        if (world.isClient && isCollided && !isRemoved) {
            if (owner == MinecraftClient.getInstance().player && GrappleModUtils.controller == null) {
                //owner?.sendMessage("Creating Controller".literal)
                val controller = GrapplingHookPhysicsController(this.id, owner!!.id, this.world)
                GrappleModUtils.controller = controller
                if (Manager.soundEffect) {
                    MinecraftClient.getInstance().soundManager.play(
                        FlyingSoundInstance(
                            player as ClientPlayerEntity
                        ) { controller.isControllerActive }
                    )
                }
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

    override fun setVelocity(entity: Entity, f: Float, g: Float, h: Float, i: Float, j: Float) {
        val k = -MathHelper.sin(g * (Math.PI / 180.0).toFloat()) * MathHelper.cos(f * (Math.PI / 180.0).toFloat())
        val l = -MathHelper.sin((f + h) * (Math.PI / 180.0).toFloat())
        val m = MathHelper.cos(g * (Math.PI / 180.0).toFloat()) * MathHelper.cos(f * (Math.PI / 180.0).toFloat())
        this.setVelocity(k.toDouble(), l.toDouble(), m.toDouble(), i, j)
        val vec3d = entity.velocity
        this.velocity = velocity.add(vec3d.x, 0.0, vec3d.z)
    }

    fun pullVehicleTowardsOwner(speed: Double = 1.5) {
        val player = owner as? PlayerEntity ?: return
        val direction = player.pos.subtract(this.vehicle?.pos ?: return).normalize()
        //Kann man noch anpassen
        val multiplySpeed = if (player.distanceTo(this.vehicle) < 2) {
            0.2
        } else {
            speed
        }
        this.vehicle?.modifyVelocity(direction.multiply(multiplySpeed))
    }

    fun pullEntityTowardsWeb(entity: Entity?, speed: Double = 1.5) {
        val direction = this.pos.subtract(entity?.pos ?: return).normalize()
        //Kann man noch anpassen
        val multiplySpeed = if (entity.distanceTo(this) < 2) {
            0.2
        } else {
            speed
        }
        entity.modifyVelocity(direction.multiply(multiplySpeed))
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
