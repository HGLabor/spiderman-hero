package gg.norisk.heroes.spiderman.grapple

import gg.norisk.heroes.spiderman.event.Events
import gg.norisk.heroes.spiderman.util.Vec
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext
import net.minecraft.world.World

/*
Full Credits to https://github.com/yyon/grapplemod
 */
object GrappleModUtils {
    fun rayTraceBlocks(entity: Entity, world: World, from: Vec, to: Vec): BlockHitResult? {
        val result: BlockHitResult = world.raycast(
            RaycastContext(
                from.toVec3d(),
                to.toVec3d(),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
            )
        )

        return if (result.type === HitResult.Type.BLOCK) result
        else null
    }

    fun isMovingSlowly(entity: Entity): Boolean {
        if (entity is ClientPlayerEntity) {
            return entity.shouldSlowDown()
        }

        return false
    }

    var controller: GrapplingHookPhysicsController? = null

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register {
            controller?.doClientTick()
        }

        Events.afterTickInputEvent.listen {
            if (controller != null) {
                controller?.receivePlayerMovementMessage(
                    it.input.movementSideways,
                    it.input.movementForward,
                    it.input.sneaking
                )
                it.input.jumping = false
                it.input.pressingBack = false
                it.input.pressingForward = false
                it.input.pressingLeft = false
                it.input.pressingRight = false
                it.input.movementForward = 0f
                it.input.movementSideways = 0f
            }
        }
    }
}
