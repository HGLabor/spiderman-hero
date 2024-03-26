package gg.norisk.heroes.spiderman.item

import gg.norisk.heroes.spiderman.entity.WebEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import kotlin.random.Random

class WebShooterItem(settings: Settings) : Item(settings) {
    override fun hasGlint(itemStack: ItemStack): Boolean {
        return true
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        world.playSound(
            null,
            player.x,
            player.y,
            player.z,
            SoundEvents.ENTITY_SPIDER_AMBIENT,
            SoundCategory.NEUTRAL,
            0.5f,
            Random.nextDouble(1.5, 2.0).toFloat()
        )
        if (!world.isClient) {
            WebEntity.removeWebs(player)

            world.playSoundFromEntity(player, SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.PLAYERS, 1f, 1f)

            val webEntity = WebEntity(world, player)
            webEntity.setItem(Items.COBWEB.defaultStack)
            webEntity.setVelocity(player, player.pitch, player.yaw, 0.0f, 2f, 1.0f)
            world.spawnEntity(webEntity)
        }
        return super.use(world, player, hand)
    }
}
