package gg.norisk.heroes.spiderman.registry

import gg.norisk.heroes.spiderman.Manager.toId
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text

object ItemRegistry {
    fun init() {
    }

    val ITEM_GROUP: ItemGroup = Registry.register(
        Registries.ITEM_GROUP, "spiderman_items".toId(), FabricItemGroup
            .builder()
            .displayName(Text.translatable("itemGroup.examplemod.spiderman_items"))
            .icon { ItemStack(Items.COBWEB) }
            .entries { _: ItemGroup.DisplayContext?, entries: ItemGroup.Entries ->
            }.build()
    )

    private fun <I : Item> registerItem(name: String, item: I): I {
        return Registry.register(Registries.ITEM, name.toId(), item)
    }
}
