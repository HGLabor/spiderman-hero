package gg.norisk.heroes.spiderman

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroConfig
import gg.norisk.heroes.spiderman.abilities.WebShoot

class SpidermanConfig : HeroConfig("Spiderman") {
}

val Spiderman by Hero("Spiderman", ::SpidermanConfig) {
    color = 0xCF2345
    ability(WebShoot)
    //getSkin { "textures/hulk_skin.png".toId() }
}

