package extrasystemreloaded.systems.upgrades.methods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import extrasystemreloaded.util.Utilities;

import java.util.Map;


public class CreditsMethod implements UpgradeMethod {
    private static final String OPTION = "ESShipExtraUpgradeApplyCredits";

    @Override
    public String getOptionId() {
        return OPTION;
    }


    @Override
    public String getOptionText(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        int level = es.getUpgrade(upgrade);
        float resourceCreditCost = getCreditCostForResources(upgrade.getResourceCosts(fm, level));
        int convenienceFee = getConvenienceCreditCost(resourceCreditCost, level, upgrade.getMaxLevel(fm.getHullSpec().getHullSize()), market);
        int creditCost = getFinalCreditCost(fm, upgrade, level, market);

        String creditCostFormatted = Misc.getFormat().format(creditCost);
        String convenienceFeeFormatted = Misc.getFormat().format(convenienceFee);
        return StringUtils.getTranslation("UpgradeMethods", "CreditsOption")
                .format("credits", creditCostFormatted)
                .format("extraTax", convenienceFeeFormatted)
                .toString();
    }

    @Override
    public String getOptionTooltip(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        return StringUtils.getString("UpgradeMethods", "CreditsUpgradeTooltip");
    }

    @Override
    public void addOption(OptionPanelAPI options, FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
    }

    @Override
    public boolean canUse(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        int level = es.getUpgrade(upgrade);
        int creditCost = getFinalCreditCost(fm, upgrade, level, market);

        return Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= creditCost;
    }

    @Override
    public boolean canShow(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        return true;
    }

    @Override
    public void apply(FleetMemberAPI fm, ExtraSystems es, Upgrade upgrade, MarketAPI market) {
        int level = es.getUpgrade(upgrade);
        int creditCost = getFinalCreditCost(fm, upgrade, level, market);

        es.putUpgrade(upgrade);
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(creditCost);
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

    /**
     * A formula that uses the market's relations with the player to determine a "convenience cost" for an upgrade.
     *
     * @param market      the market
     * @param upgradeCost the cost of the upgrade
     * @param level       the level of the upgrade
     * @param max         the max level
     * @return
     */
    private static int getConvenienceCreditCost(float upgradeCost, int level, int max, MarketAPI market) {
        float rel = market.getFaction().getRelToPlayer().getRel();
        float exp = (float) (1 + 4.5 * level / max);
        float base = 2f - 0.5f * rel;
        float additive = (float) (upgradeCost * Math.pow(base, exp));

        return (int) additive;
    }

    /**
     * Calculates the cost of the upgrade based on the resources it uses and an additional convenience cost.
     *
     * @param fm      the ship to upgrade
     * @param upgrade the upgrade
     * @param level   the level of the upgrade
     * @param market  the market
     * @return the cost
     */
    public static int getFinalCreditCost(FleetMemberAPI fm, Upgrade upgrade, int level, MarketAPI market) {
        float creditCost = getCreditCostForResources(upgrade.getResourceCosts(fm, level));
        int convenienceFee = getConvenienceCreditCost(creditCost, level, upgrade.getMaxLevel(fm.getHullSpec().getHullSize()), market);
        return (int) (creditCost + convenienceFee);
    }

    @Override
    public void modifyResourcesPanel(ESInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm, Upgrade upgrade, boolean hovered) {
        if (hovered) {
            resourceCosts.put(Commodities.CREDITS, (float) CreditsMethod.getFinalCreditCost(fm, upgrade, plugin.getExtraSystems().getUpgrade(upgrade), plugin.getMarket()));
        }
    }
}
