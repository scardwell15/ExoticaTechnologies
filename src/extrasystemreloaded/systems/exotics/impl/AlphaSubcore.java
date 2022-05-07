package extrasystemreloaded.systems.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.hullmods.ExtraSystemHM;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import extrasystemreloaded.util.Utilities;
import lombok.Getter;

import java.awt.*;
import java.util.Map;

public class AlphaSubcore extends Exotic {
    private static final String ITEM = "alpha_core";
    private static final Color[] tooltipColors = {Color.CYAN, ExtraSystemHM.infoColor};

    public static final int COST_REDUCTION_LG = 4;
    public static final int COST_REDUCTION_MED = 2;
    public static final int COST_REDUCTION_SM = 1;
    public static final int COST_REDUCTION_FIGHTER = 2;
    public static final int COST_REDUCTION_BOMBER = 2;

    @Getter private final Color mainColor = Color.cyan;

    @Override
    public boolean canApply(FleetMemberAPI fm) {
        if(fm.getFleetData() == null
                || fm.getFleetData().getFleet() == null) {
            return canApply(fm.getVariant());
        }

        if (!fm.getFleetData().getFleet().equals(Global.getSector().getPlayerFleet())) {
            if(fm.getFleetData().getFleet().getFaction().equals(Factions.HEGEMONY)
                    || fm.getFleetData().getFleet().getFaction().equals(Factions.LUDDIC_CHURCH)
                    || fm.getFleetData().getFleet().getFaction().equals(Factions.LUDDIC_PATH)) {
                return false;
            }
            return canApply(fm.getVariant());
        }

        return canApply(fm.getVariant()) && Utilities.hasItem(fm.getFleetData().getFleet().getCargo(), ITEM);
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
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ExtraSystems systems, boolean expand) {
        if (systems.hasExotic(this.getKey())) {
            if (expand) {
                StringUtils.getTranslation(this.getKey(), "longDescription")
                        .format("exoticName", this.getName())
                        .format("large", COST_REDUCTION_LG)
                        .format("medium", COST_REDUCTION_MED)
                        .format("small", COST_REDUCTION_SM)
                        .format("fighters", COST_REDUCTION_FIGHTER)
                        .format("bombers", COST_REDUCTION_BOMBER)
                        .addToTooltip(tooltip, tooltipColors);
            } else {
                tooltip.addPara(this.getName(), tooltipColors[0], 5);
            }
        }
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm) {
        resourceCosts.put(ITEM, 1f);
    }

    @Override
    public void applyExoticToShip(FleetMemberAPI fm, ShipAPI ship, float bandwidth, String id) {
        if(ship.getVariant() != null && !ship.getVariant().hasHullMod("es_alphasubcore")) {
            ship.getVariant().addMod("es_alphasubcore");
        }
    }

    /**
     * extra bandwidth added directly to ship.
     * @param fm
     * @param es
     * @return
     */
    public float getExtraBandwidth(FleetMemberAPI fm, ExtraSystems es) {
        return 50f;
    }
}
