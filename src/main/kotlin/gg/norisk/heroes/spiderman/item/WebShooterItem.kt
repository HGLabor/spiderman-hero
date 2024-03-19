package gg.norisk.heroes.spiderman.item

import gg.norisk.heroes.spiderman.entity.WebEntity
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.SnowballEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class WebShooterItem(settings: Settings) : Item(settings) {
    override fun hasGlint(itemStack: ItemStack): Boolean {
        return true
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (!world.isClient) {
            val snowballEntity = WebEntity(world, player)
            snowballEntity.setItem(Items.COBWEB.defaultStack)
            snowballEntity.setVelocity(player, player.pitch, player.yaw, 0.0f, 1.5f, 1.0f)
            world.spawnEntity(snowballEntity)
        }
        return super.use(world, player, hand)
    }
}
