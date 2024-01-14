package exoticatechnologies.ui.impl.shop.upgrades.methods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.upgrades.chips.UpgradeChipSearcher
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

class ChipMethod : DefaultUpgradeMethod() {
    override val key = "chip"
    var upgradeChipStack: CargoStackAPI? = null
    override fun getOptionText(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?
    ): String {
        return StringUtils.getString("UpgradeMethods", "ChipOption")
    }

    override fun getOptionTooltip(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?
    ): String {
        return StringUtils.getString("UpgradeMethods", "ChipOptionTooltip")
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI?): Boolean {
        market ?: return false

        if (upgradeChipStack != null) {
            val creditCost = getCreditCost(member, mods, upgrade, upgradeChipStack)
            return Global.getSector().playerFleet.cargo.credits.get() >= creditCost
        }

        return (UpgradeChipSearcher().getChips(Global.getSector().playerFleet.cargo, member, mods, upgrade).isNotEmpty()
                && super.canUse(member, mods, upgrade, market))
    }

    override fun canShow(member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI?): Boolean {
        return Utilities.hasUpgradeChip(Global.getSector().playerFleet.cargo, upgrade.key)
    }

    override fun apply(
        member: FleetMemberAPI,
        variant: ShipVariantAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?
    ): String {
        requireNotNull(upgradeChipStack) { "Missing chip stack." }

        val fleet = Global.getSector().playerFleet
        val stackPlugin = upgradeChipStack!!.plugin as UpgradeSpecialItemPlugin
        val creditCost = getCreditCost(member, mods, upgrade, upgradeChipStack)
        fleet.cargo.credits.subtract(creditCost.toFloat())
        Utilities.takeItem(upgradeChipStack)

        mods.putUpgrade(upgrade, stackPlugin.upgradeLevel)
        ShipModLoader.set(member, variant, mods)
        ExoticaTechHM.addToFleetMember(member, variant)

        return StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedSuccessfully")
            .format("name", upgrade.name)
            .format("level", mods.getUpgrade(upgrade))
            .toString()
    }

    override fun getResourceCostMap(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?,
        hovered: Boolean
    ): Map<String, Float> {
        val resourceCosts: MutableMap<String, Float> = HashMap()
        if (hovered) {
            if (upgradeChipStack == null) {
                val resourceName = StringUtils.getTranslation("ShipListDialog", "ChipName")
                    .format("name", upgrade.name)
                    .toString()
                resourceCosts[String.format("&%s", resourceName)] = 1f
                return resourceCosts
            }
            resourceCosts[Commodities.CREDITS] = getCreditCost(member, mods, upgrade, upgradeChipStack).toFloat()
            resourceCosts[Utilities.formatSpecialItem(upgradeChipStack!!.specialDataIfSpecial)] =
                1f
        }
        return resourceCosts
    }

    companion object {
        /**
         * returns the most valuable and still usable chip for the ship and its bandwidth.
         * @param fm the ship
         * @param mods the mods
         * @param upgrade the upgrade
         * @return the stack
         */
        fun getDesiredChip(fm: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade): CargoStackAPI? {
            return Utilities.getUpgradeChip(Global.getSector().playerFleet.cargo, fm, mods, upgrade)
        }

        /**
         * Sums up the floats in the map.
         *
         * @param resourceCosts
         * @return The sum.
         */
        fun getCreditCostForResources(resourceCosts: Map<String, Int>): Int {
            var creditCost = 0f
            for ((key, value) in resourceCosts) {
                creditCost += Utilities.getItemPrice(key) * value
            }
            return creditCost.toInt()
        }

        fun getCreditCost(fm: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, stack: CargoStackAPI?): Int {
            if (stack != null) {
                val plugin = stack.plugin as UpgradeSpecialItemPlugin
                val resourceCreditCost =
                    getCreditCostForResources(
                        upgrade.getResourceCosts(fm, plugin.upgradeLevel)
                    ).toFloat()
                return (resourceCreditCost * 0.33).toInt()
            }
            return 0
        }
    }
}