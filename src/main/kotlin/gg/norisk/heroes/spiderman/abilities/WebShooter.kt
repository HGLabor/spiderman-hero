package gg.norisk.heroes.spiderman.abilities

import gg.norisk.heroes.spiderman.event.Events.entityCanClimbOnEvent
import gg.norisk.heroes.spiderman.player.isSpiderman
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity

object WebShooter {
    fun initCommon() {
        entityCanClimbOnEvent.listen {
            val player = it.entity as? PlayerEntity ?: return@listen
            it.callBack.returnValue =
                it.callBack.returnValue || (it.blockState.isOf(Blocks.COBWEB) && player.isSpiderman)
        }
    }
}
