package exoticatechnologies.modifications.upgrades.methods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class ChipMethod implements UpgradeMethod {
    @Getter
    @Setter
    public CargoStackAPI upgradeChipStack = null;

    @Override
    public String getOptionText(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getString("UpgradeMethods", "ChipOption");
    }

    @Override
    public String getOptionTooltip(FleetMemberAPI fm, ShipModifications es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getString("UpgradeMethods", "ChipOptionTooltip");
    }

    @Override
    public boolean canUse(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        if (upgradeChipStack != null) {
            int creditCost = getCreditCost(fm, mods, upgrade, upgradeChipStack);
            return Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= creditCost;
        }
        return Utilities.hasUpgradeChip(fm.getFleetData().getFleet().getCargo(), upgrade.getKey());
    }

    @Override
    public boolean canShow(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        return Utilities.hasUpgradeChip(fm.getFleetData().getFleet().getCargo(), upgrade.getKey());
    }

    @Override
    public String apply(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        if (upgradeChipStack == null) {
            throw new IllegalArgumentException("Missing chip stack.");
        }

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        UpgradeSpecialItemPlugin stackPlugin = (UpgradeSpecialItemPlugin) upgradeChipStack.getPlugin();
        int creditCost = getCreditCost(fm, mods, upgrade, upgradeChipStack);

        fleet.getCargo().getCredits().subtract(creditCost);
        Utilities.takeItem(upgradeChipStack);

        mods.putUpgrade(upgrade, stackPlugin.getUpgradeLevel());
        mods.save(fm);
        ExoticaTechHM.addToFleetMember(fm);

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f);

        return StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedSuccessfully")
                .format("name", upgrade.getName())
                .format("level", mods.getUpgrade(upgrade))
                .toString();
    }

    /**
     * returns the most valuable and still usable chip for the ship and its bandwidth.
     * @param fm the ship
     * @param mods the mods
     * @param upgrade the upgrade
     * @return the stack
     */
    public static CargoStackAPI getDesiredChip(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade) {
        return Utilities.getUpgradeChip(fm.getFleetData().getFleet().getCargo(), fm, mods, upgrade);
    }

    /**
     * Sums up the floats in the map.
     *
     * @param resourceCosts
     * @return The sum.
     */
    public static int getCreditCostForResources(Map<String, Integer> resourceCosts) {
        float creditCost = 0;

        for (Map.Entry<String, Integer> resourceCost : resourceCosts.entrySet()) {
            creditCost += Utilities.getItemPrice(resourceCost.getKey()) * resourceCost.getValue();
        }
        return (int) creditCost;
    }

    public static int getCreditCost(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, CargoStackAPI stack) {
        if (stack != null) {
            UpgradeSpecialItemPlugin plugin = (UpgradeSpecialItemPlugin) stack.getPlugin();

            float resourceCreditCost = getCreditCostForResources(upgrade.getResourceCosts(fm, plugin.getUpgradeLevel()));
            int creditCost = (int) (resourceCreditCost * 0.33);

            return creditCost;
        }
        return 0;
    }

    @Override
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, Upgrade upgrade, MarketAPI market, boolean hovered) {
        Map<String, Float> resourceCosts = new HashMap<>();

        if (hovered) {
            if (upgradeChipStack == null) {
                String resourceName = StringUtils.getTranslation("ShipListDialog", "ChipName")
                        .format("name", upgrade.getName())
                        .toString();

                resourceCosts.put(String.format("&%s", resourceName), 1f);

                return resourceCosts;
            }

            resourceCosts.put(Commodities.CREDITS, (float) getCreditCost(fm, mods, upgrade, upgradeChipStack));
            resourceCosts.put(Utilities.formatSpecialItem(upgradeChipStack.getSpecialDataIfSpecial()), 1f);
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
