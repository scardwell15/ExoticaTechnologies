package exoticatechnologies.ui.impl.shop.upgrades.methods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModLoader.Companion.set
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import lombok.Getter

class CreditsMethod : DefaultUpgradeMethod() {
    @Getter
    override var key = "credits"
    override fun getOptionText(fm: FleetMemberAPI, es: ShipModifications, upgrade: Upgrade, market: MarketAPI): String {
        val level = es.getUpgrade(upgrade)
        val resourceCreditCost = upgrade.getCreditCostForResources(upgrade.getResourceCosts(fm, level)).toFloat()
        val convenienceFee =
            getConvenienceCreditCost(resourceCreditCost, level, upgrade.maxLevel, market)
        val creditCost = getFinalCreditCost(fm, upgrade, level, market)
        val creditCostFormatted = Misc.getFormat().format(creditCost.toLong())
        val convenienceFeeFormatted = Misc.getFormat().format(convenienceFee.toLong())
        return StringUtils.getTranslation("UpgradeMethods", "CreditsOption")
            .format("credits", creditCostFormatted)
            .format("extraTax", convenienceFeeFormatted)
            .toString()
    }

    override fun getOptionTooltip(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI
    ): String {
        return StringUtils.getString("UpgradeMethods", "CreditsUpgradeTooltip")
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI): Boolean {
        if (upgrade.resourceRatios.isEmpty()) return false
        val level = mods.getUpgrade(upgrade)
        val creditCost = getFinalCreditCost(member, upgrade, level, market)
        return (Global.getSector().playerFleet.cargo.credits.get() >= creditCost
                && super.canUse(member, mods, upgrade, market))
    }

    override fun apply(fm: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI): String {
        val level = mods.getUpgrade(upgrade)
        val creditCost = getFinalCreditCost(fm, upgrade, level, market)
        Global.getSector().playerFleet.cargo.credits.subtract(creditCost.toFloat())
        mods.putUpgrade(upgrade)
        set(fm, mods)
        ExoticaTechHM.addToFleetMember(fm)
        return StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedSuccessfully")
            .format("name", upgrade.name)
            .format("level", mods.getUpgrade(upgrade))
            .toString()
    }

    override fun getResourceCostMap(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI,
        hovered: Boolean
    ): Map<String, Float> {
        val resourceCosts: MutableMap<String, Float> = HashMap()
        var cost = 0f
        if (hovered) {
            cost = getFinalCreditCost(member, upgrade, mods.getUpgrade(upgrade), market).toFloat()
        }
        resourceCosts[Commodities.CREDITS] = cost
        return resourceCosts
    }

    companion object {
        /**
         * A formula that uses the market's relations with the player to determine a "convenience cost" for an upgrade.
         *
         * @param market      the market
         * @param upgradeCost the cost of the upgrade
         * @param level       the level of the upgrade
         * @param max         the max level
         * @return
         */
        private fun getConvenienceCreditCost(upgradeCost: Float, level: Int, max: Int, market: MarketAPI): Int {
            val rel = market.faction.relToPlayer.rel
            val exp = (1 + 4.5 * level / max).toFloat()
            val base = 2f - 0.5f * rel
            val additive = (upgradeCost * Math.pow(base.toDouble(), exp.toDouble())).toFloat()
            return additive.toInt()
        }

        /**
         * Calculates the cost of the upgrade based on the resources it uses and an additional convenience cost.
         *
         * @param fm      the ship to upgrade
         * @param upgrade the upgrade
         * @param level   the level of the upgrade
         * @param market  the market
         * @return the cost
         */
        fun getFinalCreditCost(fm: FleetMemberAPI, upgrade: Upgrade, level: Int, market: MarketAPI): Int {
            val creditCost = upgrade.getCreditCostForResources(upgrade.getResourceCosts(fm, level)).toFloat()
            val convenienceFee =
                getConvenienceCreditCost(creditCost, level, upgrade.maxLevel, market)
            return (creditCost + convenienceFee).toInt()
        }
    }
}