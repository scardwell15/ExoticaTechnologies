package extrasystemreloaded.systems.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import extrasystemreloaded.campaign.rulecmd.ESInteractionDialogPlugin;
import extrasystemreloaded.hullmods.ExtraSystemHM;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import extrasystemreloaded.util.Utilities;
import extrasystemreloaded.systems.exotics.Exotic;
import lombok.Getter;
import org.json.JSONException;

import java.awt.*;
import java.util.Map;

public class HangarForge extends Exotic {
    private static final String ITEM = "esr_hangarforge";
    private static final Color[] tooltipColors = {Color.GREEN, ExtraSystemHM.infoColor, ExtraSystemHM.infoColor};

    private static float RATE_DECREASE_MODIFIER = -35f;
    private static float FIGHTER_REPLACEMENT_TIME_BONUS = -15f;

    @Getter private final Color mainColor = Color.GREEN;

    @Override
    public void loadConfig() throws JSONException {
        RATE_DECREASE_MODIFIER = (float) exoticSettings.getDouble("replacementRateDecreaseSpeed");
        FIGHTER_REPLACEMENT_TIME_BONUS = (float) exoticSettings.getDouble("fighterReplacementBuff");
    }

    @Override
    public boolean canApply(FleetMemberAPI fm) {
        if (fm.getStats().getNumFighterBays().getModifiedValue() <= 0) {
            return false;
        }

        if (isNPC(fm)) {
            return canApply(fm.getVariant());
        }

        if (!fm.getFleetData().getFleet().equals(Global.getSector().getPlayerFleet())) {
            return canApply(fm.getVariant());
        }

        return canApply(fm.getVariant()) && Utilities.hasItem(fm.getFleetData().getFleet().getCargo(), ITEM);
    }

    @Override
    public String getUnableToApplyTooltip(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        return "You need a Hangar Forge to install this.";
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);
        return true;
    }

    @Override
    public boolean restoreItemsToFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.addItem(fleet, ITEM, 1);
        return true;
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ESInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm) {
        resourceCosts.put(ITEM, 1f);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ExtraSystems systems, boolean expand) {
        if (systems.hasExotic(this.getKey())) {
            if (expand) {
                StringUtils.getTranslation(this.getKey(), "longDescription")
                        .format("exoticName", this.getName())
                        .format("replacementRateIncrease", FIGHTER_REPLACEMENT_TIME_BONUS)
                        .format("rateDecreaseBuff", RATE_DECREASE_MODIFIER)
                        .addToTooltip(tooltip, tooltipColors);
            } else {
                tooltip.addPara(this.getName(), tooltipColors[0], 5);
            }
        }
    }

    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(getBuffId(), 1f + RATE_DECREASE_MODIFIER / 100f);

        float timeMult = 1f / ((100f + FIGHTER_REPLACEMENT_TIME_BONUS) / 100f);
        stats.getFighterRefitTimeMult().modifyMult(getBuffId(), timeMult);
    }
}
