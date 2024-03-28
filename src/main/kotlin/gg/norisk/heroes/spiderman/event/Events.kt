package gg.norisk.heroes.spiderman.event

import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Event
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

//Hi Enricooe wenn du das hier list müssen wir auslagern also ist aber hatte kein template
@OptIn(ExperimentalSilkApi::class)
object Events {
    open class EntityEvent(val entity: Entity)
    open class EntityCanClimbOnEvent(
        entity: Entity,
        val blockState: BlockState,
        val callBack: CallbackInfoReturnable<Boolean>
    ) : EntityEvent(entity)

    val entityCanClimbOnEvent = Event.onlySync<EntityCanClimbOnEvent>()
}
