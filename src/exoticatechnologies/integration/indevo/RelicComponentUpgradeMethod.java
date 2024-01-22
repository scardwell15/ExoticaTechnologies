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
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.ui.impl.shop.upgrades.methods.DefaultUpgradeMethod;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.FleetMemberUtils;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RelicComponentUpgradeMethod extends DefaultUpgradeMethod {
    @Getter
    public String key = "relicComponents";
    private static final String OPTION = "ETShipExtraUpgradeApplyIndEvoRelics";

    @NotNull
    @Override
    public String getOptionText(@NotNull FleetMemberAPI member, @NotNull ShipModifications mods, @NotNull Upgrade upgrade, @Nullable MarketAPI market) {
        return StringUtils.getTranslation("UpgradeMethods", "IndEvoRelicsOption")
                .format("relics", IndEvoUtil.getUpgradeRelicComponentPrice(member, upgrade, mods.getUpgrade(upgrade)))
                .toString();
    }

    @Nullable
    @Override
    public String getOptionTooltip(@NotNull FleetMemberAPI member, @NotNull ShipModifications mods, @NotNull Upgrade upgrade, @Nullable MarketAPI market) {
        return StringUtils.getTranslation("UpgradeMethods", "IndEvoRelicsTooltip")
                .format("relics", getTotalComponents(FleetMemberUtils.INSTANCE.findFleetForVariant(member.getVariant(), member), market))
                .toString();
    }

    @Override
    public boolean canUse(@NotNull FleetMemberAPI member, ShipModifications mods, @NotNull Upgrade upgrade, MarketAPI market) {
        int level = mods.getUpgrade(upgrade);
        int upgradeCost = IndEvoUtil.getUpgradeRelicComponentPrice(member, upgrade, level);
        int totalComponents = getTotalComponents(FleetMemberUtils.INSTANCE.findFleetForVariant(member.getVariant(), member), market);

        return (totalComponents - upgradeCost) >= 0
                && super.canUse(member, mods, upgrade, market);
    }

    @NotNull
    @Override
    public String apply(@NotNull FleetMemberAPI member, @NotNull ShipVariantAPI variant, ShipModifications mods, @NotNull Upgrade upgrade, MarketAPI market) {
        int level = mods.getUpgrade(upgrade);
        int upgradeCost = IndEvoUtil.getUpgradeRelicComponentPrice(member, upgrade, level);

        if (market != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE) != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo() != null) {

            CargoAPI storageCargo = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();

            upgradeCost = removeCommodityAndReturnRemainingCost(storageCargo, IndEvoUtil.RELIC_COMPONENT_ITEM_ID, upgradeCost);
        }

        CargoAPI fleetCargo = FleetMemberUtils.INSTANCE.findFleetForVariant(member.getVariant(), member).getCargo();
        if (upgradeCost > 0) {
            removeCommodity(fleetCargo, IndEvoUtil.RELIC_COMPONENT_ITEM_ID, upgradeCost);
        }

        mods.putUpgrade(upgrade);
        ShipModLoader.set(member, variant, mods);
        ExoticaTechHM.addToFleetMember(member, variant);

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f);
        return StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedSuccessfully")
                .format("name", upgrade.getName())
                .format("level", mods.getUpgrade(upgrade))
                .toString();
    }

    @NotNull
    @Override
    public Map<String, Float> getResourceCostMap(@NotNull FleetMemberAPI member, @NotNull ShipModifications mods, @NotNull Upgrade upgrade, @Nullable MarketAPI market, boolean hovered) {
        Map<String, Float> resourceCosts = new HashMap<>();

        if (hovered) {
            float cost = IndEvoUtil.getUpgradeRelicComponentPrice(member, upgrade, mods.getUpgrade(upgrade));
            resourceCosts.put(IndEvoUtil.RELIC_COMPONENT_ITEM_ID, cost);
        }

        return resourceCosts;
    }

    private Integer getTotalComponents(CampaignFleetAPI fleet, MarketAPI market) {
        return getComponentsFromFleetForUpgrade(fleet) + getComponentsFromStorageForUpgrade(market);
    }

    private int getComponentsFromFleetForUpgrade(CampaignFleetAPI fleet) {
        return Math.round(fleet.getCargo().getCommodityQuantity(IndEvoUtil.RELIC_COMPONENT_ITEM_ID));
    }

    private int getComponentsFromStorageForUpgrade(MarketAPI market) {
        int result = 0;

        boolean hasStorage = false;
        if (market != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE) != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo() != null) {
            result = Math.round(market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().getCommodityQuantity(IndEvoUtil.RELIC_COMPONENT_ITEM_ID));
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