package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.ExoticData;
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
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm, MarketAPI market) {
        if (Utilities.hasItem(fleet.getCargo(), ITEM)) {
            Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);
        } else {
            Utilities.takeItemQuantity(Misc.getStorageCargo(market), ITEM, 1);
        }
        return true;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications mods, boolean expand) {
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
    public void applyExoticToStats(String id, FleetMemberAPI fm, MutableShipStatsAPI stats, ExoticData data) {
        onInstall(fm);
    }

    /**
     * extra bandwidth added directly to ship.
     *
     * @param fm
     * @param mods
     * @param data
     * @return
     */
    public float getExtraBandwidth(FleetMemberAPI fm, ShipModifications mods, ExoticData data) {
        return 60f;
    }
}
