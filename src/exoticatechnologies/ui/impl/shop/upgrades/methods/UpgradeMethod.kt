package exoticatechnologies.ui.impl.shop.upgrades.methods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade

/**
 * An UpgradeMethod represents a way for an upgrade to be attained through the upgrade dialog.
 */
interface UpgradeMethod {
    val key: String

    /**
     * The option text
     * @return the option text
     */
    fun getOptionText(fm: FleetMemberAPI, es: ShipModifications, upgrade: Upgrade, market: MarketAPI): String

    /**
     * The option tooltip
     * @param fm fleet member to be upgraded
     * @param es modifications object
     * @param market market
     * @param upgrade
     * @return the option tooltip
     */
    fun getOptionTooltip(fm: FleetMemberAPI, es: ShipModifications, upgrade: Upgrade, market: MarketAPI): String?

    /**
     * Whether this upgrade method can be used.
     * @param fm fleet member to be upgraded
     * @param es modifications object
     * @param market market
     * @return whether the upgrade method can be used
     */
    fun canUse(fm: FleetMemberAPI, es: ShipModifications, upgrade: Upgrade, market: MarketAPI): Boolean

    /**
     * Whether to show this upgrade method.
     * @param fm fleet member to be upgraded
     * @param es modifications object
     * @param market market
     * @return whether the upgrade method can be shown
     */
    fun canShow(fm: FleetMemberAPI, es: ShipModifications, upgrade: Upgrade, market: MarketAPI): Boolean

    /**
     * Applies the upgrade to the ship. This should also take the price away from the player.
     * @param fm fleet member to be upgraded
     * @param mods modifications object
     * @param upgrade upgrade
     * @param market market
     * @return string to display briefly
     */
    fun apply(fm: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI): String

    /**
     * Get cost of resources.
     */
    fun getResourceCostMap(
        fm: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI,
        hovered: Boolean
    ): Map<String, Float>

    /**
     * Whether the upgrade method will increase bandwidth usage. If true, and a selected upgrade's bandwidth usage
     * would exceed a ship's bandwidth, it will be disabled.
     * @return whether it cares
     */
    fun usesBandwidth(): Boolean

    /**
     * Whether the upgrade method will increase upgrade level. If true, and a selected upgrade PLUS ONE would exceed
     * an upgrade's max level for that ship, it will be disabled.
     * @return whether it cares
     */
    fun usesLevel(): Boolean
}