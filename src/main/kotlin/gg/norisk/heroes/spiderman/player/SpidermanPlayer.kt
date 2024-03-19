package gg.norisk.heroes.spiderman.player

import net.minecraft.entity.Entity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.*

interface SpidermanPlayer {

}

var PlayerEntity.spidermanAnchorPoint: BlockPos?
    get() {
        return this.dataTracker.get(spidermanAnchorPointTracker).orElse(null)
    }
    set(value) {
        this.dataTracker.set(spidermanAnchorPointTracker, Optional.ofNullable(value))
    }

fun PlayerEntity.getLeashTarget(): Optional<Entity?> {
    return (dataTracker.get(leashEntityIdTracker) as OptionalInt)
        .stream()
        .mapToObj(world::getEntityById)
        .filter(Objects::nonNull)
        .findFirst()
}

fun PlayerEntity.setLeashTarget(entity: Entity) {
    this.dataTracker.set(leashEntityIdTracker, OptionalInt.of(entity.id))
}


val spidermanAnchorPointTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS)
val leashEntityIdTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_INT)
