package exoticatechnologies.modifications.upgrades.methods;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
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
    public String getOptionText(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market);

    /**
     * The option tooltip
     * @param fm fleet member to be upgraded
     * @param es modifications object
     * @param market market
     * @param upgrade
     * @return the option tooltip
     */
    public String getOptionTooltip(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market);

    /**
     * Whether this upgrade method can be used.
     * @param fm fleet member to be upgraded
     * @param es modifications object
     * @param market market
     * @return whether the upgrade method can be used
     */
    public boolean canUse(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market);

    /**
     * Whether to show this upgrade method.
     * @param fm fleet member to be upgraded
     * @param es modifications object
     * @param market market
     * @return whether the upgrade method can be shown
     */
    public boolean canShow(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market);

    /**
     * Applies the upgrade to the ship. This should also take the price away from the player.
     * @param dialog
     * @param plugin
     * @param mods modifications object
     * @param market market
     * @param fm fleet member to be upgraded
     */
    public void apply(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, ShipModifications mods, Upgrade upgrade, MarketAPI market, FleetMemberAPI fm);

    /**
     * Modify the resources panel.
     */
    public void modifyResourcesPanel(ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm, Upgrade upgrade, boolean hovered);
}
