package extrasystemreloaded.systems.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import extrasystemreloaded.hullmods.ExtraSystemHM;
import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StringUtils;
import lombok.Getter;

import java.awt.*;

public class SubsumedBetaCore extends Exotic {
    private static final Color[] tooltipColors = {new Color(0x6AA900), ExtraSystemHM.infoColor};

    @Getter
    private final Color mainColor = new Color(0x6AA900);

    @Override
    public String getTextDescription() {
        return this.getDescription();
    }

    @Override
    public boolean shouldShow(FleetMemberAPI fm, ExtraSystems es, MarketAPI market) {
        return false;
    }

    @Override
    public boolean canApply(FleetMemberAPI fm) {
        if (fm.getFleetData() == null
                || fm.getFleetData().getFleet() == null) {
            return false;
        }

        if (fm.getFleetData().getFleet().getFaction().toString().equals(Factions.OMEGA)) {
            return super.canApply(fm.getVariant());
        }

        return false;
    }

    public String getUnableToApplyTooltip(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        return "You aren't an omega, right?";
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        return true;
    }

    @Override
    public boolean restoreItemsToFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        return true;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ExtraSystems systems, boolean expand) {
        if (systems.hasExotic(this.getKey())) {
            if (expand) {
                StringUtils.getTranslation(this.getKey(), "description")
                        .addToTooltip(tooltip, tooltipColors);
            } else {
                tooltip.addPara(this.getName(), tooltipColors[0], 5);
            }
        }
    }

    /**
     * extra bandwidth added directly to ship.
     *
     * @param fm
     * @param es
     * @return
     */
    public float getExtraBandwidth(FleetMemberAPI fm, ExtraSystems es) {
        return 35f;
    }
}
