package gg.norisk.heroes.spiderman.grapple

import gg.norisk.heroes.common.events.afterTickInputEvent
import gg.norisk.heroes.common.events.keyEvent
import gg.norisk.heroes.spiderman.Manager.toId
import gg.norisk.heroes.spiderman.util.Vec
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import net.silkmc.silk.network.packet.c2sPacket

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

    val webEntityDiscardPacket = c2sPacket<Int>("web-entity-discard-packet".toId())

    var controller: GrapplingHookPhysicsController? = null
    private var lastJumpPressTime = 0L
    private const val doublePressThreshold = 500L

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register {
            controller?.doClientTick()
        }

        //Double Press Key Logic maybe move?
        keyEvent.listen {
            //TODO maybe resetten wenn action == 2 also gedrückt halten
            if (it.client.options.jumpKey.matchesKey(it.key, it.scanCode) && it.action == 1) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastJumpPressTime < doublePressThreshold) {
                    // Taste wurde zweimal schnell hintereinander gedrückt
                    // Füge hier die Aktion ein, die ausgeführt werden soll
                    //it.client.player?.sendMessage("Doppelklick erkannt!".literal)
                    controller?.doJump = true
                }
                lastJumpPressTime = currentTime
            }
        }

        afterTickInputEvent.listen {
            if (controller != null) {
                controller?.receivePlayerMovementMessage(
                    it.input.movementSideways, it.input.movementForward, it.input.sneaking
                )
                it.input.sneaking = false
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
