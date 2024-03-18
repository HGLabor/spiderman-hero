package gg.norisk.heroes.spiderman.item

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.text.literal

class WebShooterItem(settings: Settings) : Item(settings) {
    override fun hasGlint(itemStack: ItemStack): Boolean {
        return true
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        // Der Ankerpunkt für das Schwingen (hardcoded für dieses Beispiel)
        val swingAnchor = Vec3d(0.0, 100.0, 0.0) // Ein hoher Punkt über dem Spieler
        val ropeLength = 30.0 // Hardcoded für dieses Beispiel
        if (!world.isClient) {
            val playerPos = player.pos
            val directionToAnchor = swingAnchor.subtract(playerPos)
            val distanceToAnchor = directionToAnchor.length()

            // Stelle sicher, dass der Spieler sich nicht außerhalb der Seillänge bewegt
            if (distanceToAnchor > ropeLength) {
                // Berechne die neue Position basierend auf der Pendelbewegung
                val normalizedDirection = directionToAnchor.normalize()
                val newPosition = swingAnchor.subtract(normalizedDirection.multiply(ropeLength))

                // Ermittle die Bewegungsgeschwindigkeit für die Pendelbewegung
                val pendulumVelocity = calculatePendulumVelocity(playerPos, newPosition, world)

                // Wende die berechnete Geschwindigkeit an, um den Spieler zu bewegen
                player.modifyVelocity(pendulumVelocity)
                player.sendMessage("Swinging like a pendulum on Server".literal)
            }
        }
        return super.use(world, player, hand)
    }

    private fun calculatePendulumVelocity(currentPosition: Vec3d, newPosition: Vec3d, world: World): Vec3d {
        // Dies ist eine sehr vereinfachte Berechnung und soll nur als Beispiel dienen.
        // Für eine realistischere Bewegung müssten die Physik des Pendels und externe Kräfte wie Schwerkraft berücksichtigt werden.
        val movementDirection = newPosition.subtract(currentPosition).normalize()
        // Annahme: konstante Geschwindigkeit für die Demonstration
        val speed = 1.0
        return movementDirection.multiply(speed)
    }
}
