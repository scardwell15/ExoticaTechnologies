package exoticatechnologies.integration.indevo;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModLoader;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.ui2.impl.mods.upgrades.methods.DefaultUpgradeMethod;
import exoticatechnologies.util.FleetMemberUtils;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ShipComponentUpgradeMethod extends DefaultUpgradeMethod {
    @Getter
    public String key = "shipComponents";
    @NotNull
    @Override
    public String getOptionText(@NotNull FleetMemberAPI member, @NotNull ShipModifications mods, @NotNull Upgrade upgrade, @Nullable MarketAPI market) {
        return StringUtils.getTranslation("UpgradeMethods", "IndEvoComponentsOption")
                .format("components", IndEvoUtil.getUpgradeShipComponentPrice(member, upgrade, mods.getUpgrade(upgrade)))
                .toString();
    }

    @Nullable
    @Override
    public String getOptionTooltip(@NotNull FleetMemberAPI member, @NotNull ShipVariantAPI variant, @NotNull ShipModifications mods, @NotNull Upgrade upgrade, @Nullable MarketAPI market) {
        return StringUtils.getTranslation("UpgradeMethods", "IndEvoComponentsTooltip")
                .format("components", getTotalComponents(FleetMemberUtils.INSTANCE.findFleetForVariant(variant, member), market))
                .toString();
    }

    @Override
    public boolean canUse(@NotNull FleetMemberAPI member, ShipVariantAPI variant, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        int level = mods.getUpgrade(upgrade);
        int upgradeCost = IndEvoUtil.getUpgradeShipComponentPrice(member, upgrade, level);
        int totalComponents = getTotalComponents(FleetMemberUtils.INSTANCE.findFleetForVariant(variant, member), market);

        return (totalComponents - upgradeCost) >= 0
                && super.canUse(member, variant, mods, upgrade, market);
    }

    @NotNull
    @Override
    public String apply(@NotNull FleetMemberAPI member, @NotNull ShipVariantAPI variant, ShipModifications mods, Upgrade upgrade, MarketAPI market) {
        int level = mods.getUpgrade(upgrade);
        int upgradeCost = IndEvoUtil.getUpgradeShipComponentPrice(member, upgrade, level);

        if (market != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE) != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo() != null) {

            CargoAPI storageCargo = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();

            upgradeCost = removeCommodityAndReturnRemainingCost(storageCargo, IndEvoUtil.SHIP_COMPONENT_ITEM_ID, upgradeCost);
        }

        CargoAPI fleetCargo = FleetMemberUtils.INSTANCE.findFleetForVariant(variant, member).getCargo();
        if (upgradeCost > 0) {
            removeCommodity(fleetCargo, IndEvoUtil.SHIP_COMPONENT_ITEM_ID, upgradeCost);
        }


        mods.putUpgrade(upgrade);
        ShipModLoader.set(member, variant, mods);
        ExoticaTechHM.addToFleetMember(member, variant);

        return StringUtils.getTranslation("Upgrades", "UpgradePerformedSuccessfully")
                .format("name", upgrade.getName())
                .format("level", mods.getUpgrade(upgrade))
                .toString();
    }

    @NotNull
    @Override
    public Map<String, Float> getResourceCostMap(@NotNull FleetMemberAPI member, @NotNull ShipModifications mods, @NotNull Upgrade upgrade, @Nullable MarketAPI market, boolean hovered) {
        Map<String, Float> resourceCosts = new HashMap<>();

        if (hovered) {
            float cost = IndEvoUtil.getUpgradeShipComponentPrice(member, upgrade, mods.getUpgrade(upgrade));
            resourceCosts.put(IndEvoUtil.SHIP_COMPONENT_ITEM_ID, cost);
        }


        return resourceCosts;
    }

    private Integer getTotalComponents(CampaignFleetAPI fleet, MarketAPI market) {
        return getComponentsFromFleetForUpgrade(fleet) + getComponentsFromStorageForUpgrade(market);
    }

    private int getComponentsFromFleetForUpgrade(CampaignFleetAPI fleet) {
        return Math.round(fleet.getCargo().getCommodityQuantity(IndEvoUtil.SHIP_COMPONENT_ITEM_ID));
    }

    private int getComponentsFromStorageForUpgrade(MarketAPI market) {
        int result = 0;

        if (market != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE) != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo() != null) {
            result = Math.round(market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().getCommodityQuantity(IndEvoUtil.SHIP_COMPONENT_ITEM_ID));
        }

        return result;
    }

    private void removeCommodity(CargoAPI cargo, String id, float cost) {
        cargo.removeCommodity(id, cost);
    }

    private int removeCommodityAndReturnRemainingCost(CargoAPI cargo, String id, float cost) {
        float current = cargo.getCommodityQuantity(id);
        float taken = Math.min(current, cost);
        cargo.removeCommodity(id, taken);
        return (int) (cost - taken);
    }

    @Override
    public boolean shouldLoad() {
        return Global.getSettings().getModManager().isModEnabled("IndEvo");
    }
}