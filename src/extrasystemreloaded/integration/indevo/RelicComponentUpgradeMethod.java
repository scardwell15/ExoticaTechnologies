package extrasystemreloaded.integration.indevo;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.systems.upgrades.methods.UpgradeMethod;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;

import java.util.Map;

public class RelicComponentUpgradeMethod implements UpgradeMethod {
    private static final String OPTION = "ESShipExtraUpgradeApplyIndEvoRelics";

    @Override
    public String getOptionId() {
        return OPTION;
    }

    @Override
    public String getOptionText(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getTranslation("UpgradeMethods", "IndEvoRelicsOption")
                .format("relics", IndEvoUtil.getUpgradeRelicComponentPrice(fm, upgrade, es.getUpgrade(upgrade)))
                .toString();
    }

    @Override
    public String getOptionTooltip(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getTranslation("UpgradeMethods", "IndEvoRelicsTooltip")
                .format("relics", getTotalComponents(fm.getFleetData().getFleet(), market))
                .toString();
    }

    @Override
    public void addOption(OptionPanelAPI options, FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
    }

    @Override
    public boolean canUse(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        int level = es.getUpgrade(upgrade);
        int upgradeCost = IndEvoUtil.getUpgradeRelicComponentPrice(fm, upgrade, level);
        int totalComponents = getTotalComponents(fm.getFleetData().getFleet(), market);

        return (totalComponents - upgradeCost) > 0;
    }

    @Override
    public boolean canShow(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        return true;
    }

    @Override
    public void apply(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        int level = es.getUpgrade(upgrade);
        int upgradeCost = IndEvoUtil.getUpgradeShipComponentPrice(fm, upgrade, level);
        int totalComponents = getTotalComponents(fm.getFleetData().getFleet(), market);

        if (market != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE) != null
                && market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo() != null) {

            CargoAPI storageCargo = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();

            upgradeCost = removeCommodityAndReturnRemainingCost(storageCargo, IndEvoUtil.RELIC_COMPONENT_ITEM_ID, upgradeCost);
        }

        CargoAPI fleetCargo = fm.getFleetData().getFleet().getCargo();
        if (upgradeCost > 0) {
            removeCommodity(fleetCargo, IndEvoUtil.RELIC_COMPONENT_ITEM_ID, upgradeCost);
        }

        es.putUpgrade(upgrade);
    }

    @Override
    public void modifyResourcesPanel(ESInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm, Upgrade upgrade, boolean hovered) {
        float cost = 0;
        if (hovered) {
            cost = IndEvoUtil.getUpgradeRelicComponentPrice(fm, upgrade, plugin.getExtraSystems().getUpgrade(upgrade));
        }

        resourceCosts.put(IndEvoUtil.RELIC_COMPONENT_ITEM_ID, cost);
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
}