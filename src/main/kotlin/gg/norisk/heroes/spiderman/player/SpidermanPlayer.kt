package gg.norisk.heroes.spiderman.player

import net.minecraft.entity.Entity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import java.util.*
import kotlin.random.Random

interface SpidermanPlayer {

}

fun PlayerEntity.playGenericSpidermanSound() {
    world.playSoundFromEntity(
        null, this, SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.PLAYERS, 0.5f,
        Random.nextDouble(1.5, 3.0).toFloat()
    )
}

var PlayerEntity.gravity: Float
    get() {
        return this.dataTracker.get(gravityTracker)
    }
    set(value) {
        this.dataTracker.set(gravityTracker, value)
    }

var PlayerEntity.isSpiderman: Boolean
    get() {
        return this.dataTracker.get(spidermanTracker)
    }
    set(value) {
        this.dataTracker.set(spidermanTracker, value)
    }

var PlayerEntity.isSwinging: Boolean
    get() {
        return this.dataTracker.get(swingingTracker)
    }
    set(value) {
        this.dataTracker.set(swingingTracker, value)
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

val gravityTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
val swingingTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val spidermanTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val spidermanAnchorPointTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS)
val leashEntityIdTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_INT)
