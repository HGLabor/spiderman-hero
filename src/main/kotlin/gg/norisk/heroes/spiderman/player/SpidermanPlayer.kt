package gg.norisk.heroes.spiderman.player

import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import kotlin.random.Random

fun PlayerEntity.playGenericSpidermanSound() {
    world.playSoundFromEntity(
        null, this, SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.PLAYERS, 0.5f,
        Random.nextDouble(1.5, 3.0).toFloat()
    )
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

val swingingTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val spidermanTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val spidermanAnchorPointTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS)
val leashEntityIdTracker =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_INT)
