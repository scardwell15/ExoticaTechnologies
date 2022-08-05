package exoticatechnologies.modifications.upgrades.methods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;

import java.util.HashMap;
import java.util.Map;


public class ResourcesMethod implements UpgradeMethod {
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
        if (upgrade.getResourceRatios().isEmpty()) return false;

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
    public String apply(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        Map<String, Integer> upgradeCosts = upgrade.getResourceCosts(fm, mods.getUpgrade(upgrade));

        Utilities.takeResources(fm.getFleetData().getFleet(), market, upgradeCosts);

        mods.putUpgrade(upgrade);
        mods.save(fm);
        ExoticaTechHM.addToFleetMember(fm);

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f);
        return StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedSuccessfully")
                .format("name", upgrade.getName())
                .format("level", mods.getUpgrade(upgrade))
                .toString();
    }

    @Override
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market, boolean hovered) {
        Map<String, Float> resourceCosts = new HashMap<>();

        if (hovered) {
            for(Map.Entry<String, Integer> entry : upgrade.getResourceCosts(fm, mods.getUpgrade(upgrade)).entrySet()) {
                resourceCosts.put(entry.getKey(), Float.valueOf(entry.getValue()));
            }
        }

        return resourceCosts;
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
