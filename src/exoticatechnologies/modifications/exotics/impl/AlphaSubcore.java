package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AlphaSubcore extends HullmodExotic {
    private static final String ITEM = "alpha_core";

    public AlphaSubcore(String key, JSONObject settingsObj) {
        super(key, settingsObj, "et_alphasubcore", "AlphaSubcore", Color.cyan);
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM)
                || (Misc.getStorageCargo(market) != null && Utilities.hasItem(Misc.getStorageCargo(market), ITEM));
    }

    @Override
    public boolean canApply(FleetMemberAPI member, ShipModifications mods) {
        if(member.getFleetData() == null
                || member.getFleetData().getFleet() == null) {
            return canApplyToVariant(member.getVariant());
        }

        if (!Misc.isPlayerOrCombinedContainingPlayer(member.getFleetData().getFleet())) {
            if(member.getFleetData().getFleet().getFaction().getId().equals(Factions.HEGEMONY)
                    || member.getFleetData().getFleet().getFaction().getId().equals(Factions.LUDDIC_CHURCH)
                    || member.getFleetData().getFleet().getFaction().getId().equals(Factions.LUDDIC_PATH)) {
                return false;
            }
            return canApplyToVariant(member.getVariant());
        }

        return canApplyToVariant(member.getVariant());
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm, MarketAPI market) {
        if (Utilities.hasItem(fleet.getCargo(), ITEM)) {
            Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);
        } else {
            Utilities.takeItemQuantity(Misc.getStorageCargo(market), ITEM, 1);
        }
        return true;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), "longDescription")
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        Map<String, Float> resourceCosts = new HashMap<>();
        resourceCosts.put(ITEM, 1f);
        return resourceCosts;
    }


    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        onInstall(fm);
        stats.getDynamic().getStat(Stats.DEPLOYMENT_POINTS_MOD).modifyPercent(getBuffId(), 20, "Alpha Subcore");
    }

    /**
     * extra bandwidth added directly to ship.
     * @param fm
     * @param es
     * @return
     */
    public float getExtraBandwidth(FleetMemberAPI fm, ShipModifications es) {
        return 60f;
    }
}
