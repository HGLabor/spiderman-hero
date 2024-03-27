package gg.norisk.heroes.spiderman.network

import gg.norisk.heroes.spiderman.Manager.toId
import kotlinx.serialization.ExperimentalSerializationApi
import net.silkmc.silk.network.packet.c2sPacket

@OptIn(ExperimentalSerializationApi::class)
object Packets {
    val webShooterPacketC2S = c2sPacket<Unit>("webshooter".toId())
}
