package exoticatechnologies.modifications.upgrades.methods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;

import java.util.Map;

public class ChipMethod implements UpgradeMethod {
    @Override
    public String getOptionText(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        int creditCost = getCreditCost(fm, mods, upgrade);

        String creditCostFormatted = Misc.getFormat().format(creditCost);
        return StringUtils.getTranslation("UpgradeMethods", "ChipOption")
                .format("credits", creditCostFormatted)
                .toString();
    }

    @Override
    public String getOptionTooltip(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getString("UpgradeMethods", "ChipOptionTooltip");
    }

    @Override
    public boolean canUse(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        if (Utilities.hasUpgradeChip(fm.getFleetData().getFleet().getCargo(), upgrade.getKey())) {
            int creditCost = getCreditCost(fm, mods, upgrade);
            return Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= creditCost;
        }
        return false;
    }

    @Override
    public boolean canShow(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        return Utilities.hasUpgradeChip(fm.getFleetData().getFleet().getCargo(), upgrade.getKey());
    }

    @Override
    public void apply(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, ShipModifications mods, Upgrade upgrade, MarketAPI market, FleetMemberAPI fm) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        CargoStackAPI stack = Utilities.getUpgradeChip(fm.getFleetData().getFleet().getCargo(), upgrade.getKey());
        UpgradeSpecialItemPlugin stackPlugin = (UpgradeSpecialItemPlugin) stack.getPlugin();
        int creditCost = getCreditCost(fm, mods, upgrade);

        Utilities.takeUpgradeChip(fleet.getCargo(), upgrade.getKey(), stackPlugin.getUpgradeLevel());
        mods.putUpgrade(upgrade, stackPlugin.getUpgradeLevel());
        fleet.getCargo().getCredits().subtract(creditCost);

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f);
        StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedSuccessfully")
                .format("name", upgrade.getName())
                .format("level", mods.getUpgrade(upgrade))
                .addToTextPanel(dialog.getTextPanel());
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
        CargoStackAPI stack = Utilities.getUpgradeChip(fm.getFleetData().getFleet().getCargo(), upgrade.getKey());
        if (stack != null) {
            UpgradeSpecialItemPlugin plugin = (UpgradeSpecialItemPlugin) stack.getPlugin();

            float resourceCreditCost = getCreditCostForResources(upgrade.getResourceCosts(fm, plugin.getUpgradeLevel()));
            int creditCost = (int) (resourceCreditCost * 0.33);

            return creditCost;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public void modifyResourcesPanel(ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm, Upgrade upgrade, boolean hovered) {
        if (hovered) {
            resourceCosts.put(Commodities.CREDITS, (float) getCreditCost(fm, plugin.getExtraSystems(), upgrade));

            CargoStackAPI stack = Utilities.getUpgradeChip(fm.getFleetData().getFleet().getCargo(), upgrade.getKey());
            if (stack != null) {
                UpgradeSpecialItemPlugin stackPlugin = (UpgradeSpecialItemPlugin) stack.getPlugin();
                resourceCosts.put(ETInteractionDialogPlugin.formatSpecialItem(stack.getSpecialDataIfSpecial()), 1f);
            }
        }
    }
}
