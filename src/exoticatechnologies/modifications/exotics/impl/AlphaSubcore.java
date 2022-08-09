package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AlphaSubcore extends Exotic {
    private static final String ITEM = "alpha_core";

    public static final int COST_REDUCTION_LG = 4;
    public static final int COST_REDUCTION_MED = 2;
    public static final int COST_REDUCTION_SM = 1;
    public static final int COST_REDUCTION_FIGHTER = 2;
    public static final int COST_REDUCTION_BOMBER = 2;

    @Getter private final Color mainColor = Color.cyan;

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM);
    }

    @Override
    public boolean canApply(FleetMemberAPI fm) {
        if(fm.getFleetData() == null
                || fm.getFleetData().getFleet() == null) {
            return canApply(fm.getVariant());
        }

        if (!Misc.isPlayerOrCombinedContainingPlayer(fm.getFleetData().getFleet())) {
            if(fm.getFleetData().getFleet().getFaction().getId().equals(Factions.HEGEMONY)
                    || fm.getFleetData().getFleet().getFaction().getId().equals(Factions.LUDDIC_CHURCH)
                    || fm.getFleetData().getFleet().getFaction().getId().equals(Factions.LUDDIC_PATH)) {
                return false;
            }
            return canApply(fm.getVariant());
        }

        return canApply(fm.getVariant());
    }

    public String getUnableToApplyTooltip(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        return StringUtils.getTranslation(this.getKey(), "needItem")
                .format("itemName", Global.getSettings().getSpecialItemSpec(ITEM).getName())
                .toString();
    }

    @Override
    public boolean restoreItemsToFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.addItem(fleet, ITEM, 1);
        return true;
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);
        return true;
    }

    @Override
    public void onInstall(FleetMemberAPI fm) {
        if(fm.getVariant() != null && !fm.getVariant().hasHullMod("et_alphasubcore")) {
            fm.getVariant().addMod("et_alphasubcore");
        }
    }

    @Override
    public void onDestroy(FleetMemberAPI fm) {
        if (fm.getVariant() != null) {
            fm.getVariant().removeMod("AlphaSubcoreHM");
        }
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), "longDescription")
                    .format("large", COST_REDUCTION_LG)
                    .format("medium", COST_REDUCTION_MED)
                    .format("small", COST_REDUCTION_SM)
                    .format("fighters", COST_REDUCTION_FIGHTER)
                    .format("bombers", COST_REDUCTION_BOMBER)
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
    }

    @Override
    public void applyExoticToShip(FleetMemberAPI fm, ShipAPI ship, float bandwidth, String id) {
        onInstall(fm);
    }

    /**
     * extra bandwidth added directly to ship.
     * @param fm
     * @param es
     * @return
     */
    public float getExtraBandwidth(FleetMemberAPI fm, ShipModifications es) {
        return 50f;
    }
}
