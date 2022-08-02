package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;

import java.awt.*;

public class SubsumedAlphaCore extends Exotic {

    @Getter
    private final Color mainColor = Color.cyan;

    @Override
    public String getTextDescription() {
        return this.getDescription();
    }

    @Override
    public boolean shouldShow(FleetMemberAPI fm, ShipModifications es, MarketAPI market) {
        return false;
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return true;
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

    @Override
    public boolean canDropFromFleets() {
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
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), "description")
                    .addToTooltip(tooltip, title);
        }
    }

    /**
     * extra bandwidth added directly to ship.
     *
     * @param fm
     * @param es
     * @return
     */
    public float getExtraBandwidth(FleetMemberAPI fm, ShipModifications es) {
        return 50f;
    }
}
