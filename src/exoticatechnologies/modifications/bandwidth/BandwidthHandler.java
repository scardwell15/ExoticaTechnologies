package exoticatechnologies.modifications.bandwidth;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exoticatechnologies.ETModPlugin;
import exoticatechnologies.modifications.ShipModifications;
import org.magiclib.util.MagicSettings;

import java.util.Map;

public class BandwidthHandler {
    private static final int UPGRADE_OPTION_ORDER = 0;

    public static boolean canUpgrade(ShipModifications buff, FleetMemberAPI selectedShip) {
        return buff == null || buff.canUpgradeBandwidth(selectedShip);
    }

    public static boolean isAbleToPayForBandwidthUpgrade(CampaignFleetAPI fleet, float cost) {
        return cost <= fleet.getCargo().getCredits().get() || ETModPlugin.isDebugUpgradeCosts();
    }

    public static float getMarketBandwidthMult(MarketAPI currMarket) {
        Map<String, Float> marketBonuses = MagicSettings.getFloatMap("exoticatechnologies", "industryBandwidthPurchaseBonuses");

        float bandwidthMult = 1;
        for (Industry industry : currMarket.getIndustries()) {
            if (marketBonuses.containsKey(industry.getId())) {
                bandwidthMult += marketBonuses.get(industry.getId());
            }
        }
        return bandwidthMult;
    }

    public static float getBandwidthUpgradePrice(FleetMemberAPI selectedShip, float shipBandwidth, float upgradeBandwidthMult) {
        float deployCost = selectedShip.getBaseDeployCost();
        float shipBaseValue = selectedShip.getBaseValue();
        if (shipBaseValue > 450000) {
            shipBaseValue = 225000;
        } else {
            shipBaseValue = (float) (shipBaseValue - (1d / 900000d) * Math.pow(shipBaseValue, 2));
        }
        float bandwidthMultFactor = 1 - (upgradeBandwidthMult / (upgradeBandwidthMult + 10));

        return Math.round(shipBaseValue * (float) Math.pow(shipBandwidth / 70f, 2) / (2f + 6f * bandwidthMultFactor) * 100f) / 100f;
    }
}
