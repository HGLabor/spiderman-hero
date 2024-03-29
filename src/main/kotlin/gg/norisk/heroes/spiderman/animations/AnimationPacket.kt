package gg.norisk.heroes.spiderman.animations

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AnimationPacket(
    @Serializable(with = UUIDSerializer::class)
    val playerUuid: UUID,
    val animation: String
)
