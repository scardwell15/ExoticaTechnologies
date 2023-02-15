package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticData;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TierIIIDriveSystem extends Exotic {
    private static float CARGO_TO_FUEL_PERCENT = 75f;
    private static float BURN_BONUS_FUEL_REQ = 66f;
    private static float BURN_BONUS = 2f;

    @Getter
    private final Color color = new Color(0xFFA2E4);

    public TierIIIDriveSystem(String key, JSONObject settings) {
        super(key, settings);
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM);
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm, MarketAPI market) {
        Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);

        return true;
    }

    @Override
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        Map<String, Float> resourceCosts = new HashMap<>();
        resourceCosts.put(
                "&" + StringUtils.getTranslation("ShipListDialog", "ChipName")
                        .format("name", getName())
                        .toStringNoFormats(), 1f);
        return resourceCosts;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications mods, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), "longDescription")
                    .format("cargoToFuelPercent", CARGO_TO_FUEL_PERCENT)
                    .format("burnBonusFuelReq", BURN_BONUS_FUEL_REQ)
                    .format("burnBonus", BURN_BONUS)
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public void applyExoticToStats(String id, FleetMemberAPI fm, MutableShipStatsAPI stats, ExoticData data) {
        int addedFuelCap = (int) (fm.getCargoCapacity() * CARGO_TO_FUEL_PERCENT / 100f);
        stats.getCargoMod().modifyMult(this.getBuffId(), 1 - (CARGO_TO_FUEL_PERCENT / 100f));
        stats.getFuelMod().modifyFlat(this.getBuffId(), addedFuelCap);
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, ShipModifications mods, Float amount, ExoticData data) {
        if (member.getFleetData() != null && member.getFleetData().getFleet() != null) {
            checkBuff(member.getFleetData().getFleet());

        }
    }

    @Override
    public void onDestroy(FleetMemberAPI member) {
        if (member.getFleetData() != null && member.getFleetData().getFleet() != null) {
            removeBuff(member.getFleetData().getFleet().getStats());
        }
    }

    private void checkBuff(CampaignFleetAPI fleet) {
        MutableFleetStatsAPI fleetStats = fleet.getStats();
        if (fleet.getCargo().getFreeFuelSpace() < fleet.getCargo().getMaxFuel() * 0.25) {
            if (fleetStats.getFleetwideMaxBurnMod().getFlatBonus(this.getBuffId()) == null) {
                fleetStats.getFleetwideMaxBurnMod().modifyFlat(this.getBuffId(), BURN_BONUS, this.getName());
            }
        } else {
            removeBuff(fleetStats);
        }
    }

    private void removeBuff(MutableFleetStatsAPI fleetStats) {
        if (fleetStats.getFleetwideMaxBurnMod().getFlatBonus(this.getBuffId()) != null) {
            fleetStats.getFleetwideMaxBurnMod().unmodify(this.getBuffId());
        }
    }
}
