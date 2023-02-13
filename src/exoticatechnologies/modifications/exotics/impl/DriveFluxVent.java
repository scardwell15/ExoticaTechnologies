package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import data.scripts.util.MagicUI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.util.RenderUtils;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import exoticatechnologies.util.states.StateWithNext;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
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

    @Getter
    private final Color color = new Color(0x9D62C4);

    public DriveFluxVent(String key, JSONObject settings) {
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
    public void advanceInCombatAlways(ShipAPI ship, float bandwidth) {
        VentState state = getVentState(ship);
        state.advanceAlways(ship);
    }

    @Override
    public void advanceInCombatUnpaused(ShipAPI ship, float amount, float bandwidth) {
        VentState state = getVentState(ship);
        state.advance(ship, amount);
    }

    private static String STATE_KEY = "et_drivefluxvent_state";

    private VentState getVentState(ShipAPI ship) {
        VentState state = (VentState) ship.getCustomData().get(STATE_KEY);
        if (state == null) {
            state = new ReadyState();
            ship.setCustomData(STATE_KEY, state);
        }
        return state;
    }

    private String getStatusBarText() {
        return StringUtils.getString(this.getKey(), "statusBarText");
    }

    private abstract class VentState extends StateWithNext {
        VentState() {
            super(STATE_KEY);
        }

        public abstract void advanceAlways(ShipAPI ship);
    }

    private static Color READY_STATE_COLOR = new Color(170, 140, 220);

    private class ReadyState extends VentState {
        @Override
        protected void init(@NotNull ShipAPI ship) {
            ship.getMutableStats().getAcceleration().unmodify(DriveFluxVent.this.getBuffId());
            ship.getMutableStats().getDeceleration().unmodify(DriveFluxVent.this.getBuffId());
            ship.getMutableStats().getMaxSpeed().unmodify(DriveFluxVent.this.getBuffId());
        }

        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
            if (ship.getFluxTracker().isVenting()) {
                if (ship.getCurrFlux() > ship.getMaxFlux() * FLUX_LEVEL_REQUIRED / 100f) {
                    setNextState(ship);
                }
            }
        }

        private boolean isReady(ShipAPI ship) {
            return ship.getCurrFlux() > ship.getMaxFlux() * FLUX_LEVEL_REQUIRED / 100f;
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            Color renderColor = RenderUtils.getAliveUIColor();
            if (isReady(ship)) {
                renderColor = READY_STATE_COLOR;
            }
            MagicUI.drawInterfaceStatusBar(ship, 1f, renderColor, renderColor, 0, DriveFluxVent.this.getStatusBarText(), -1);
        }

        @Override
        protected float getDuration() {
            return 0f;
        }

        @NotNull
        @Override
        protected StateWithNext getNextState() {
            return new BuffedState();
        }
    }

    private class BuffedState extends VentState {
        @Override
        protected void init(@NotNull ShipAPI ship) {
            ship.getEngineController().fadeToOtherColor(DriveFluxVent.this.getBuffId(), new Color(255, 75, 255), null, 1f, 0.75f);
        }

        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
            if (ship.getFluxTracker().isVenting()) {
                getInterval().setElapsed(0f);
            }

            ship.getEngineController().fadeToOtherColor(DriveFluxVent.this.getBuffId(), new Color(255, 75, 255), null, this.getProgressRatio(), 0.75f);

            float velocityDir = VectorUtils.getFacing(ship.getVelocity()) - ship.getFacing();
            if (Math.abs(velocityDir) < 15f) {
                ship.getMutableStats().getAcceleration().modifyPercent(DriveFluxVent.this.getBuffId(), 50f);
                ship.getMutableStats().getDeceleration().modifyPercent(DriveFluxVent.this.getBuffId(), -50f);
                ship.getMutableStats().getMaxSpeed().modifyPercent(DriveFluxVent.this.getBuffId(), FORWARD_SPEED_INCREASE);
            } else {
                ship.getMutableStats().getAcceleration().unmodify(DriveFluxVent.this.getBuffId());
                ship.getMutableStats().getDeceleration().unmodify(DriveFluxVent.this.getBuffId());
                ship.getMutableStats().getMaxSpeed().unmodify(DriveFluxVent.this.getBuffId());
            }
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f - this.getProgressRatio(), RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 0, DriveFluxVent.this.getStatusBarText(), -1);
        }

        @Override
        protected float getDuration() {
            return SPEED_BUFF_TIME;
        }

        @Override
        protected boolean intervalExpired(@NotNull ShipAPI ship) {
            setNextState(ship);
            return true;
        }

        @NotNull
        @Override
        protected StateWithNext getNextState() {
            return new ReadyState();
        }
    }
}
