package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import exoticatechnologies.modifications.exotics.Exotic;
import lombok.Getter;
import org.json.JSONException;
import org.lazywizard.lazylib.VectorUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DriveFluxVent extends Exotic {
    private static final String ITEM = "et_drivevent";

    private static float VENT_SPEED_INCREASE = 30f;
    private static int FORWARD_SPEED_INCREASE = 50;
    private static float FLUX_LEVEL_REQUIRED = 40f;
    private static float SPEED_BUFF_TIME = 4f;

    @Getter private final Color mainColor = new Color(0x9D62C4);

    @Override
    public void loadConfig() throws JSONException {
        VENT_SPEED_INCREASE = (float) exoticSettings.getDouble("ventSpeedIncrease");
        FORWARD_SPEED_INCREASE = (int) exoticSettings.getInt("forwardSpeedIncrease");
        FLUX_LEVEL_REQUIRED = (float) exoticSettings.getDouble("fluxRequiredForBuff");
        SPEED_BUFF_TIME = (float) exoticSettings.getDouble("buffDuration");
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM);
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
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        Map<String, Float> resourceCosts = new HashMap<>();
        resourceCosts.put(Utilities.formatSpecialItem(ITEM), 1f);
        return resourceCosts;
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (expand) {
            StringUtils.getTranslation(this.getKey(), "longDescription")
                    .format("ventBonus", VENT_SPEED_INCREASE)
                    .format("speedThreshold", FLUX_LEVEL_REQUIRED)
                    .format("speedBonus", String.valueOf(FORWARD_SPEED_INCREASE))
                    .format("speedBonusTime", SPEED_BUFF_TIME)
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        stats.getVentRateMult().modifyPercent(this.getBuffId(), VENT_SPEED_INCREASE);
    }

    private String getDriveStateId(ShipAPI ship) {
        return ship.getId() + this.getKey() + "state";
    }

    private String getIntervalId(ShipAPI ship) {
        return ship.getId() + this.getKey() + "interval";
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount, float bandwidth) {
        Map<String, Object> customData = Global.getCombatEngine().getCustomData();
        if(!customData.containsKey(getDriveStateId(ship))) {
            customData.put(getDriveStateId(ship), DriveState.NONE);
        }

        DriveState state = (DriveState) customData.get(getDriveStateId(ship));

        if(state == DriveState.VENTING || state == DriveState.OUT) {
            float velocityDir = VectorUtils.getFacing(ship.getVelocity()) - ship.getFacing();
            if(Math.abs(velocityDir) < 25f) {
                ship.getMutableStats().getAcceleration().modifyPercent(this.getBuffId(), 50f);
                ship.getMutableStats().getMaxSpeed().modifyPercent(this.getBuffId(), FORWARD_SPEED_INCREASE);

                Global.getCombatEngine().maintainStatusForPlayerShip(this.getBuffId(),
                        "graphics/icons/hullsys/infernium_injector.png",
                        StringUtils.getString(this.getKey(), "statusTitle"),
                        StringUtils.getString(this.getKey(), "statusSpeedBonus"),
                        false);
            } else {
                ship.getMutableStats().getAcceleration().unmodify(this.getBuffId());
                ship.getMutableStats().getMaxSpeed().unmodify(this.getBuffId());
            }
        }

        if(ship.getFluxTracker().isVenting()) {
            if(ship.getCurrFlux() > ship.getMaxFlux() * FLUX_LEVEL_REQUIRED / 100f) {
                ship.getEngineController().fadeToOtherColor(this.getBuffId(), new Color(255, 75, 255), null, 1f, 0.75f);

                if (state != DriveState.VENTING) {
                    customData.put(getDriveStateId(ship), DriveState.VENTING);
                }
            }
        } else {
            if(state == DriveState.VENTING) {
                customData.put(getDriveStateId(ship), DriveState.OUT);
                customData.put(getIntervalId(ship), new IntervalUtil(SPEED_BUFF_TIME, SPEED_BUFF_TIME));
                state = DriveState.OUT;
            }

            if(state == DriveState.OUT) {

                IntervalUtil intervalUtil = (IntervalUtil) customData.get(getIntervalId(ship));
                intervalUtil.advance(amount);

                float ratio = 1f - (intervalUtil.getElapsed() / intervalUtil.getIntervalDuration());
                ship.getEngineController().fadeToOtherColor(this.getBuffId(), new Color(255, 75, 255), null, 0.25f + 0.5f * ratio, 0.75f);

                if(intervalUtil.intervalElapsed()) {
                    customData.put(getDriveStateId(ship), DriveState.NONE);

                    ship.getMutableStats().getAcceleration().unmodify(this.getBuffId());
                    ship.getMutableStats().getMaxSpeed().unmodify(this.getBuffId());
                }
            }
        }
    }

    private enum DriveState {
        VENTING,
        OUT,
        NONE
    }
}
