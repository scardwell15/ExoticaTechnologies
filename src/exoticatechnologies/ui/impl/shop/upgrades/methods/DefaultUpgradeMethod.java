package exoticatechnologies.ui.impl.shop.upgrades.methods;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;

/**
 * An UpgradeMethod represents a way for an upgrade to be attained through the upgrade dialog.
 */
public abstract class DefaultUpgradeMethod implements UpgradeMethod {
    /**
     * Whether this upgrade method can be used.
     * @param member fleet member to be upgraded
     * @param mods modifications object
     * @param market market
     * @return whether the upgrade method can be used
     */
    public boolean canUse(FleetMemberAPI member, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        return upgrade.canApply(member, mods)
                && (usesLevel() && mods.getUpgrade(upgrade) + 1 <= upgrade.getMaxLevel(member))
                && (usesBandwidth() && mods.getUsedBandwidth() + upgrade.getBandwidthUsage() <= mods.getBandwidthWithExotics(member));
    }

    /**
     * Whether to show this upgrade method.
     * @param mods fleet member to be upgraded
     * @param member modifications object
     * @param market market
     * @return whether the upgrade method can be shown
     */
    public boolean canShow(FleetMemberAPI mods, ShipModifications member, Upgrade upgrade, MarketAPI market) {
        return true;
    }

    @Override
    public boolean usesBandwidth() {
        return true;
    }

    @Override
    public boolean usesLevel() {
        return true;
    }
}
