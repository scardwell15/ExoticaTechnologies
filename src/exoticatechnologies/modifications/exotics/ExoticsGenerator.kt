package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.util.WeightedRandomPicker
import exoticatechnologies.config.FactionConfig
import exoticatechnologies.config.FactionConfigLoader.Companion.getFactionConfig
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.util.Utilities
import org.apache.log4j.Logger
import java.util.*

object ExoticsGenerator {
    private val log: Logger = Logger.getLogger(FactionConfig::class.java)

    @JvmStatic
    fun generate(fm: FleetMemberAPI, faction: String?, mods: ShipModifications): ETExotics {
        val config = getFactionConfig(faction!!)
        val allowedExotics: Map<Exotic, Float> = config.allowedExotics
        var exoticChance = config.exoticChance.toFloat()

        val exotics = mods.exotics
        val smodCount = Utilities.getSModCount(fm)
        exoticChance *= (1 + smodCount).toFloat()

        val random = ShipModFactory.getRandom()
        val rolledChance = random.nextFloat()
        if (rolledChance < exoticChance) {
            val perExoticMult = 1 + smodCount * 0.5f


            val exoticPicker = getPicker(random, allowedExotics)
            while (!exoticPicker.isEmpty) {
                val exotic = exoticPicker.pick(random)!!
                if (exotic.canApply(fm, mods)) {
                    val roll = random.nextFloat()
                    val factionExoticWeight = allowedExotics[exotic]!!
                    val calculatedWeight = perExoticMult * factionExoticWeight

                    if (roll < calculatedWeight) {
                        exotics.putExotic(exotic)
                    }
                }
                exoticPicker.remove(exotic)
            }
        }
        return exotics
    }

    private fun getPicker(random: Random, allowedExotics: Map<Exotic, Float>): WeightedRandomPicker<Exotic?> {
        val exoticPicker = WeightedRandomPicker<Exotic?>(random)
        allowedExotics.forEach { (exotic, factionChance) ->
            exoticPicker.add(exotic, exotic.getSalvageChance(factionChance))
        }
        return exoticPicker
    }
}