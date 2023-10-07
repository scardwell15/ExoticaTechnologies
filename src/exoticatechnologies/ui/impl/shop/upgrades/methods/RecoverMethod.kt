package exoticatechnologies.ui.impl.shop.upgrades.methods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import lombok.Getter

class RecoverMethod : UpgradeMethod {
    @Getter
    override var key = "recover"
    override fun getOptionText(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?
    ): String {
        return StringUtils.getTranslation("UpgradeMethods", "RecoverOption")
            .toString()
    }

    override fun getOptionTooltip(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?
    ): String {
        return StringUtils.getTranslation("UpgradeMethods", "RecoverOptionTooltip").toString()
    }

    override fun canShow(member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI?): Boolean {
        return mods.hasUpgrade(upgrade)
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI?): Boolean {
        market ?: return false

        if (mods.hasUpgrade(upgrade)) {
            val creditCost = getCreditCost(member, mods, upgrade)
            return Global.getSector().playerFleet.cargo.credits.get() >= creditCost
        }
        return false
    }

    override fun canUseIfMarketIsNull(): Boolean {
        return false
    }

    override fun apply(member: FleetMemberAPI, variant: ShipVariantAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI?): String {
        val fleet = Global.getSector().playerFleet
        val creditCost = getCreditCost(member, mods, upgrade)
        val stack = Utilities.getUpgradeChip(fleet.cargo, upgrade.key, mods.getUpgrade(upgrade))
        if (stack != null) {
            stack.add(1f)
        } else {
            fleet.cargo.addSpecial(upgrade.getNewSpecialItemData(mods.getUpgrade(upgrade)), 1f)
        }
        fleet.cargo.credits.subtract(creditCost.toFloat())

        mods.removeUpgrade(upgrade)
        ShipModLoader.set(member, variant, mods)
        ExoticaTechHM.addToFleetMember(member, variant)

        return StringUtils.getString("UpgradesDialog", "UpgradeRecoveredSuccessfully")
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
            resourceCosts[Commodities.CREDITS] = getCreditCost(member, mods, upgrade).toFloat()
            var resourceName = StringUtils.getTranslation("ShipListDialog", "ChipName")
                .format("name", upgrade.name)
                .toString()
            if (mods.hasUpgrade(upgrade)) {
                resourceName = StringUtils.getTranslation("ShipListDialog", "UpgradeChipWithLevelText")
                    .format("upgradeName", upgrade.name)
                    .format("level", mods.getUpgrade(upgrade))
                    .toString()
            }
            resourceCosts[String.format("&%s", resourceName)] = -1f
        }
        return resourceCosts
    }

    override fun usesBandwidth(): Boolean {
        return false
    }

    override fun usesLevel(): Boolean {
        return false
    }

    override fun shouldLoad(): Boolean {
        return true
    }

    companion object {
        /**
         * Sums up the floats in the map.
         *
         * @param resourceCosts resource cost map
         * @return The sum.
         */
        private fun getCreditCostForResources(resourceCosts: Map<String, Int>): Int {
            var creditCost = 0f
            for ((key, value) in resourceCosts) {
                creditCost += Utilities.getItemPrice(key) * value
            }
            return creditCost.toInt()
        }

        fun getCreditCost(fm: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade): Int {
            val resourceCreditCost =
                getCreditCostForResources(upgrade.getResourceCosts(fm, mods.getUpgrade(upgrade))).toFloat()
            return (resourceCreditCost * 0.166).toInt()
        }
    }
}