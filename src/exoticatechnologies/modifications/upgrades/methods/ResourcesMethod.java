package exoticatechnologies.modifications.upgrades.methods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;

import java.util.Map;


public class ResourcesMethod implements UpgradeMethod {
    private static final String OPTION = "ESShipExtraUpgradeApplyResources";

    @Override
    public String getOptionText(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getString("UpgradeMethods", "ResourcesOption");
    }

    @Override
    public String getOptionTooltip(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market) {
        return null;
    }

    @Override
    public boolean canUse(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market) {
        Map<String, Integer> upgradeCosts = upgrade.getResourceCosts(fm, es.getUpgrade(upgrade));
        Map<String, Integer> totalStacks = Utilities.getTotalResources(fm.getFleetData().getFleet(), market, upgradeCosts.keySet());

        boolean canUpgrade = true;
        for (Map.Entry<String, Integer> upgradeCost : upgradeCosts.entrySet()) {
            int remaining = totalStacks.get(upgradeCost.getKey()) - upgradeCost.getValue();
            if (remaining < 0) {
                canUpgrade = false;
                remaining = 0;
            }
            totalStacks.put(upgradeCost.getKey(), remaining);
        }

        return canUpgrade;
    }

    @Override
    public boolean canShow(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market) {
        return true;
    }

    @Override
    public void apply(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, ShipModifications mods, Upgrade upgrade, MarketAPI market, FleetMemberAPI fm) {
        Map<String, Integer> upgradeCosts = upgrade.getResourceCosts(fm, mods.getUpgrade(upgrade));

        Utilities.takeResources(fm.getFleetData().getFleet(), market, upgradeCosts);

        mods.putUpgrade(upgrade);


        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f);
        StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedSuccessfully")
                .format("name", upgrade.getName())
                .format("level", mods.getUpgrade(upgrade))
                .addToTextPanel(dialog.getTextPanel());
    }

    @Override
    public void modifyResourcesPanel(ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm, Upgrade upgrade, boolean hovered) {
        if(hovered) {
            for(Map.Entry<String, Integer> entry : upgrade.getResourceCosts(fm, plugin.getExtraSystems().getUpgrade(upgrade)).entrySet()) {
                resourceCosts.put(entry.getKey(), Float.valueOf(entry.getValue()));
            }
        }
    }
}
