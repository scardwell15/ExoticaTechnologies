package extrasystemreloaded.systems.upgrades.methods;

import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import extrasystemreloaded.util.Utilities;

import java.util.Map;


public class ResourcesMethod implements UpgradeMethod {
    private static final String OPTION = "ESShipExtraUpgradeApplyResources";

    @Override
    public String getOptionId() {
        return OPTION;
    }

    @Override
    public String getOptionText(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getString("UpgradeMethods", "ResourcesOption");
    }

    @Override
    public String getOptionTooltip(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        return null;
    }

    @Override
    public void addOption(OptionPanelAPI options, FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
    }

    @Override
    public boolean canUse(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
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
    public boolean canShow(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        return true;
    }

    @Override
    public void apply(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        Map<String, Integer> upgradeCosts = upgrade.getResourceCosts(fm, es.getUpgrade(upgrade));

        Utilities.takeResources(fm.getFleetData().getFleet(), market, upgradeCosts);

        es.putUpgrade(upgrade);
    }

    @Override
    public void modifyResourcesPanel(ESInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm, Upgrade upgrade, boolean hovered) {
        if(hovered) {
            for(Map.Entry<String, Integer> entry : upgrade.getResourceCosts(fm, plugin.getExtraSystems().getUpgrade(upgrade)).entrySet()) {
                resourceCosts.put(entry.getKey(), Float.valueOf(entry.getValue()));
            }
        }
    }
}
