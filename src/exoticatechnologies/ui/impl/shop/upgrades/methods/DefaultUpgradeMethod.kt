package exoticatechnologies.ui.impl.shop.upgrades.methods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade

/**
 * An UpgradeMethod represents a way for an upgrade to be modified through the upgrade dialog.
 */
abstract class DefaultUpgradeMethod : UpgradeMethod {
    /**
     * Whether this upgrade method can be used.
     * @param member fleet member to be upgraded
     * @param mods modifications object
     * @param market market
     * @return whether the upgrade method can be used
     */
    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI?): Boolean {
        return upgrade.canApply(member, mods) && usesLevel() && mods.getUpgrade(upgrade) + 1 <= upgrade.maxLevel
                && usesBandwidth()
                && mods.getUsedBandwidth() + upgrade.bandwidthUsage <= mods.getBandwidthWithExotics(member)
    }

    /**
     * Whether to show this upgrade method.
     * @param mods fleet member to be upgraded
     * @param mods modifications object
     * @param market market
     * @return whether the upgrade method can be shown
     */
    override fun canShow(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?
    ): Boolean {
        return true
    }

    override fun canUseIfMarketIsNull(): Boolean {
        return false
    }

    override fun usesBandwidth(): Boolean {
        return true
    }

    override fun usesLevel(): Boolean {
        return true
    }

    override fun shouldLoad(): Boolean {
        return true
    }
}