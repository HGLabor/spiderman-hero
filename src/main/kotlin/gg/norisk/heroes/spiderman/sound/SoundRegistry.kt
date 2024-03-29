package gg.norisk.heroes.spiderman.sound

import gg.norisk.heroes.spiderman.Manager.toId
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent

object SoundRegistry {
    var SPIDERMAN = Registry.register(Registries.SOUND_EVENT, "spiderman".toId(), SoundEvent.of("spiderman".toId()))

    fun init() {
    }
}
