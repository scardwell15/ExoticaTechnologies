package exoticatechnologies.modifications.upgrades.methods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;

import java.util.Map;

public class RecoverMethod implements UpgradeMethod {
    @Override
    public String getOptionText(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        int creditCost = getCreditCost(fm, mods, upgrade);

        String creditCostFormatted = Misc.getFormat().format(creditCost);
        return StringUtils.getTranslation("UpgradeMethods", "RecoverOption")
                .format("credits", creditCostFormatted)
                .toString();
    }

    @Override
    public String getOptionTooltip(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getTranslation("UpgradeMethods", "RecoverOptionTooltip").toString();
    }

    @Override
    public boolean canShow(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        return mods.hasUpgrade(upgrade);
    }

    @Override
    public boolean canUse(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        if (mods.hasUpgrade(upgrade)) {
            int creditCost = getCreditCost(fm, mods, upgrade);
            return Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= creditCost;
        }
        return false;
    }

    @Override
    public void apply(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, ShipModifications mods, Upgrade upgrade, MarketAPI market, FleetMemberAPI fm) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        ShipAPI.HullSize hullSize = fm.getHullSpec().getHullSize();

        int creditCost = getCreditCost(fm, mods, upgrade);

        CargoStackAPI stack = Utilities.getUpgradeChip(fleet.getCargo(), upgrade.getKey(), mods.getUpgrade(upgrade));
        if (stack != null) {
            stack.add(1);
        } else {
            fleet.getCargo().addSpecial(upgrade.getNewSpecialItemData(mods.getUpgrade(upgrade)), 1);
        }

        mods.removeUpgrade(upgrade);
        fleet.getCargo().getCredits().subtract(creditCost);

        mods.save(fm);
        ExoticaTechHM.addToFleetMember(fm);

        textPanel.addParagraph(StringUtils.getString("UpgradesDialog", "UpgradeRecoveredSuccessfully"));
    }

    /**
     * Sums up the floats in the map.
     *
     * @param resourceCosts
     * @return The sum.
     */
    private static int getCreditCostForResources(Map<String, Integer> resourceCosts) {
        float creditCost = 0;

        for (Map.Entry<String, Integer> resourceCost : resourceCosts.entrySet()) {
            creditCost += Utilities.getItemPrice(resourceCost.getKey()) * resourceCost.getValue();
        }
        return (int) creditCost;
    }

    private static int getCreditCost(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade) {
        float resourceCreditCost = getCreditCostForResources(upgrade.getResourceCosts(fm, mods.getUpgrade(upgrade)));
        int creditCost = (int) (resourceCreditCost * 0.166);

        return creditCost;
    }

    @Override
    public void modifyResourcesPanel(ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm, Upgrade upgrade, boolean hovered) {
        if (hovered) {
            resourceCosts.put(Commodities.CREDITS, (float) getCreditCost(fm, plugin.getExtraSystems(), upgrade));
        }
    }
}
