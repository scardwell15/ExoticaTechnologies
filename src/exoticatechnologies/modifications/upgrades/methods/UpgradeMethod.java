package exoticatechnologies.modifications.upgrades.methods;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.ShipModifications;

import java.util.Map;

/**
 * An UpgradeMethod represents a way for an upgrade to be attained through the upgrade dialog.
 */
public interface UpgradeMethod {
    /**
     * The option text
     * @return the option text
     */
    String getOptionText(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market);

    /**
     * The option tooltip
     * @param fm fleet member to be upgraded
     * @param es modifications object
     * @param market market
     * @param upgrade
     * @return the option tooltip
     */
    String getOptionTooltip(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market);

    /**
     * Whether this upgrade method can be used.
     * @param fm fleet member to be upgraded
     * @param es modifications object
     * @param market market
     * @return whether the upgrade method can be used
     */
    boolean canUse(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market);

    /**
     * Whether to show this upgrade method.
     * @param fm fleet member to be upgraded
     * @param es modifications object
     * @param market market
     * @return whether the upgrade method can be shown
     */
    boolean canShow(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market);

    /**
     * Applies the upgrade to the ship. This should also take the price away from the player.
     * @param fm fleet member to be upgraded
     * @param mods modifications object
     * @param upgrade upgrade
     * @param market market
     * @return string to display briefly
     */
    String apply(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market);

    /**
     * Get cost of resources.
     */
    Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market, boolean hovered);
}
