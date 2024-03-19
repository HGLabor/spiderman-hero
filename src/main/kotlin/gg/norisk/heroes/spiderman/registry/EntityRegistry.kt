package gg.norisk.heroes.spiderman.registry

import gg.norisk.heroes.spiderman.Manager.toId
import gg.norisk.heroes.spiderman.entity.WebEntity
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object EntityRegistry {
    val WEB: EntityType<WebEntity> = registerMob("web", ::WebEntity, 0.3f, 0.3f)

    fun registerEntityAttributes() {
    }

    private fun createGenericEntityAttributes(): DefaultAttributeContainer.Builder {
        return MobEntity.createLivingAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.80000000298023224)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
            .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.1)
    }

    private fun <T : Entity> registerMob(
        name: String, entity: EntityType.EntityFactory<T>,
        width: Float, height: Float
    ): EntityType<T> {
        val dimension = EntityDimensions.changing(width, height)
        return Registry.register(
            Registries.ENTITY_TYPE,
            name.toId(),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, entity)
                .dimensions(dimension)
                .trackRangeChunks(4096)
                .build()
        )
    }

    fun init() {
        registerEntityAttributes()
    }
}
