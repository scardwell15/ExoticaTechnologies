package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.ExoticData;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;
import org.json.JSONObject;

import java.awt.*;

public class SubsumedBetaCore extends Exotic {

    @Getter
    private final Color color = new Color(0x6AA900);

    public SubsumedBetaCore(String key, JSONObject settings) {
        super(key, settings);
    }

    @Override
    public String getTextDescription() {
        return this.getDescription();
    }

    @Override
    public boolean shouldShow(FleetMemberAPI member, ShipModifications mods, MarketAPI market) {
        return false;
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return true;
    }

    @Override
    public boolean canApply(FleetMemberAPI member, ShipModifications mods) {
        if (member.getFleetData() == null
                || member.getFleetData().getFleet() == null) {
            return false;
        }

        if (member.getFleetData().getFleet().getFaction().toString().equals(Factions.OMEGA)) {
            return super.canApplyToVariant(member.getVariant());
        }

        return false;
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm, MarketAPI market) {
        return true;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications mods, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), "description")
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public boolean canDropFromFleets() {
        return false;
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
        return 35f;
    }
}
