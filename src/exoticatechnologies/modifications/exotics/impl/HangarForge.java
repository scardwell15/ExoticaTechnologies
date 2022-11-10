package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import exoticatechnologies.modifications.exotics.Exotic;
import lombok.Getter;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HangarForge extends Exotic {
    private static final String ITEM = "et_hangarforge";

    private static float RATE_DECREASE_MODIFIER = -35f;
    private static float FIGHTER_REPLACEMENT_TIME_BONUS = -15f;

    @Getter private final Color color = Color.GREEN;

    public HangarForge(String key, JSONObject settings) {
        super(key, settings);
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM);
    }

    @Override
    public boolean canApply(FleetMemberAPI member) {
        if (member.getStats() != null) {
            if (member.getStats().getNumFighterBays().getModifiedInt() > 0) {
                return canApply(member.getVariant());
            }
        }
        return false;
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm, MarketAPI market) {
        Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);
        return true;
    }

    @Override
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        Map<String, Float> resourceCosts = new HashMap<>();
        resourceCosts.put(Utilities.formatSpecialItem(ITEM), 1f);
        return resourceCosts;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), "longDescription")
                    .format("replacementRateIncrease", FIGHTER_REPLACEMENT_TIME_BONUS)
                    .format("rateDecreaseBuff", RATE_DECREASE_MODIFIER)
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(getBuffId(), 1f + RATE_DECREASE_MODIFIER / 100f);

        float timeMult = 1f / ((100f + FIGHTER_REPLACEMENT_TIME_BONUS) / 100f);
        stats.getFighterRefitTimeMult().modifyMult(getBuffId(), timeMult);
    }
}
