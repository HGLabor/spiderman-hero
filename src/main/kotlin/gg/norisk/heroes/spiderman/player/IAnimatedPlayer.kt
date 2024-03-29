package gg.norisk.heroes.spiderman.player

import dev.kosmx.playerAnim.api.layered.IAnimation
import dev.kosmx.playerAnim.api.layered.ModifierLayer

interface IAnimatedPlayer {
    fun hero_getModAnimation(): ModifierLayer<IAnimation>
}
