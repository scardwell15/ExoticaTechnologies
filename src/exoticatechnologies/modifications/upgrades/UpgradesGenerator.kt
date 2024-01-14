package exoticatechnologies.modifications.upgrades

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.util.WeightedRandomPicker
import exoticatechnologies.config.FactionConfigLoader
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.util.Utilities
import lombok.extern.log4j.Log4j
import java.util.*

@Log4j
object UpgradesGenerator {
    @JvmStatic
    fun generate(member: FleetMemberAPI, mods: ShipModifications, context: ShipModFactory.GenerationContext): ETUpgrades {
        val config = context.factionConfig!!
        val allowedUpgrades = config.allowedUpgrades
        var upgradeChance = config.upgradeChance.toFloat() * getUpgradeChance(member)

        if (member.fleetData != null && member.fleetData.fleet != null) {
            if (member.fleetData.fleet.memoryWithoutUpdate.contains("\$exotica_upgradeMult")) {
                upgradeChance *= member.fleetData.fleet.memoryWithoutUpdate.getFloat("\$exotica_upgradeMult")
            }
        }

        val upgrades = mods.upgrades

        val smodCount = Utilities.getSModCount(member)
        upgradeChance *= (1 + smodCount).toFloat()

        val random = ShipModFactory.random
        if (random.nextFloat() < upgradeChance) {

            var usableBandwidth = mods.getBandwidthWithExotics(member)
            val perUpgradeMult = 2f + smodCount * 0.5f
            val upgradePicker = getPicker(random, allowedUpgrades, member, mods, upgrades, usableBandwidth)
            while (random.nextFloat() < usableBandwidth / 100f * perUpgradeMult && !upgradePicker.isEmpty) {
                var upgrade: Upgrade? = null
                while (upgrade == null && !upgradePicker.isEmpty) {
                    upgrade = upgradePicker.pick()

                    if (!canPickUpgrade(member, mods, upgrades, upgrade, usableBandwidth)) {
                        upgradePicker.remove(upgrade)
                        upgrade = null
                    }
                }

                if (upgrade != null) {
                    if (random.nextFloat() < (upgrade.spawnChance * (1f + 0.2f * smodCount) * allowedUpgrades[upgrade]!! * context.upgradeChanceMult)) {
                        upgrades.addUpgrades(upgrade, 1)
                        upgradePicker.setWeight(upgradePicker.items.indexOf(upgrade), upgradePicker.getWeight(upgrade) * (1.75f + 0.5f * smodCount))
                    }
                    usableBandwidth -= upgrade.bandwidthUsage
                }
            }
        }
        return upgrades
    }

    private fun getUpgradeChance(member: FleetMemberAPI): Float {
        val sizeFactor: Float = when (member.hullSpec.hullSize) {
            ShipAPI.HullSize.CAPITAL_SHIP -> 1.33f
            ShipAPI.HullSize.CRUISER -> 1.22f
            ShipAPI.HullSize.DESTROYER -> 1.11f
            else -> 1.0f
        }

        return sizeFactor
    }

    fun getPicker(random: Random, allowedUpgrades: Map<Upgrade, Float>): WeightedRandomPicker<Upgrade> {
        val upgradePicker = WeightedRandomPicker<Upgrade>(random)
        allowedUpgrades
            .forEach { (upgrade, factionChance) ->
                upgradePicker.add(upgrade, upgrade.spawnChance * factionChance)
            }
        return upgradePicker
    }

    fun getPicker(random: Random, allowedUpgrades: Map<Upgrade, Float>, member: FleetMemberAPI, mods: ShipModifications, upgrades: ETUpgrades, usableBandwidth: Float): WeightedRandomPicker<Upgrade> {
        val upgradePicker = WeightedRandomPicker<Upgrade>(random)
        allowedUpgrades
            .filterKeys { canPickUpgrade(member, mods, upgrades, it, usableBandwidth) }
            .forEach { (upgrade, factionChance) ->
            upgradePicker.add(upgrade, upgrade.spawnChance * factionChance * upgrade.getCalculatedWeight(member, mods))
        }
        return upgradePicker
    }

    fun getDefaultPicker(random: Random): WeightedRandomPicker<Upgrade> {
        val upgradePicker = WeightedRandomPicker<Upgrade>(random)
        FactionConfigLoader.getDefaultFactionUpgrades()
            .forEach { (upgrade, factionChance) ->
                upgradePicker.add(upgrade, upgrade.spawnChance * factionChance)
            }
        return upgradePicker
    }

    private fun canPickUpgrade(member: FleetMemberAPI, mods: ShipModifications, upgrades: ETUpgrades, upgrade: Upgrade, usableBandwidth: Float): Boolean {
        if (member.shipName == null && !upgrade.shouldAffectModule(null, null)) return false
        return upgrade.maxLevel > upgrades.getUpgrade(upgrade)
                && upgrade.canApply(member, mods)
                && (usableBandwidth - upgrade.bandwidthUsage) >= 0f
    }

    private fun hasLeveledUpgrades(upgradePicker: WeightedRandomPicker<Upgrade>, member: FleetMemberAPI, upgrades: ETUpgrades): Boolean {
        return upgradePicker.items.any {
            upgrades.getUpgrade(it) > 0 && it.maxLevel > upgrades.getUpgrade(it)
        }
    }
}