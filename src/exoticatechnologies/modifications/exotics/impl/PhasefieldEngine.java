package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
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
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PhasefieldEngine extends Exotic {
    private static final String ITEM = "et_phaseengine";

    private static int PHASE_RESET_INTERVAL = 6;
    private static int INVULNERABLE_INTERVAL = 3;

    private static float PHASE_COOLDOWN_PERCENT_REDUCTION = -50f;
    private static float PHASE_COST_PERCENT_REDUCTION = -75f;
    private static float PHASE_COST_PERCENT_IF_NEGATIVE = -100f;
    private static float PHASE_COST_IF_ZERO = 20f;

    @Getter
    private final Color color = new Color(0xA94EFF);

    public PhasefieldEngine(String key, JSONObject settings) {
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
                    .format("phaseCostReduction", String.valueOf(1 - Math.abs(PHASE_COST_PERCENT_REDUCTION / 100f)))
                    .format("phaseResetTime", PHASE_RESET_INTERVAL)
                    .format("noDamageTime", INVULNERABLE_INTERVAL)
                    .format("zeroFluxCost", Misc.getRounded(PHASE_COST_IF_ZERO))
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        if (fm.getHullSpec().getShieldSpec().getPhaseCost() == 0) {
            stats.getPhaseCloakActivationCostBonus().modifyFlat(getBuffId() + "base", PHASE_COST_IF_ZERO / 100f);
        } else if (fm.getHullSpec().getShieldSpec().getPhaseCost() < 0) {
            stats.getPhaseCloakActivationCostBonus().modifyMult(getBuffId() + "base", -1f);
        }
        stats.getPhaseCloakActivationCostBonus().modifyMult(getBuffId(), 1f - Math.abs(PHASE_COST_PERCENT_REDUCTION / 100f));
    }

    @Override
    public void applyExoticToShip(FleetMemberAPI fm, ShipAPI ship, float bandwidth, String id) {
        ship.getMutableStats().getPhaseCloakActivationCostBonus().unmodify("phase_anchor");
    }

    private String getInvulverableId(ShipAPI ship) {
        return String.format("%s_%s_invulnerable", this.getBuffId(), ship.getId());
    }

    private IntervalUtil getInvulnerableInterval(ShipAPI ship) {
        Object val = Global.getCombatEngine().getCustomData().get(getInvulverableId(ship));
        if (val != null) {
            return (IntervalUtil) val;
        }
        return null;
    }

    private String getTimesPhasedId(ShipAPI ship) {
        return String.format("%s_%s_timesphased", this.getBuffId(), ship.getId());
    }

    private int getTimesPhasedInInterval(ShipAPI ship) {
        Object val = Global.getCombatEngine().getCustomData().get(getTimesPhasedId(ship));
        if (val != null) {
            return (int) val;
        }
        return 0;
    }

    private void addToTimesPhased(ShipAPI ship) {
        Global.getCombatEngine().getCustomData().put(getTimesPhasedId(ship), getTimesPhasedInInterval(ship) + 1);
    }

    private void removeTimesPhased(ShipAPI ship) {
        Global.getCombatEngine().getCustomData().remove(getTimesPhasedId(ship));
    }

    @Override
    public void advanceInCombatAlways(ShipAPI ship, float bandwidth) {
        if (ship.getPhaseCloak() == null) {
            return;
        }

        FieldState state = getState(ship);
        state.advanceAlways(ship);
    }

    @Override
    public void advanceInCombatUnpaused(ShipAPI ship, float amount, float bandwidth) {
        if (ship.getPhaseCloak() == null) {
            return;
        }

        FieldState state = getState(ship);
        state.advance(ship, amount);
    }

    // damage listener
    private class ET_PhasefieldEngineListener implements DamageTakenModifier {
        private final ShipAPI ship;

        public ET_PhasefieldEngineListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damageAPI, Vector2f point, boolean shieldHit) {
            if (target == this.ship) {
                IntervalUtil interval = getInvulnerableInterval(this.ship);
                if (interval != null) {
                    damageAPI.getModifier().modifyMult(PhasefieldEngine.this.getBuffId(), 0.66f * (INVULNERABLE_INTERVAL - interval.getElapsed()) / INVULNERABLE_INTERVAL);
                    return PhasefieldEngine.this.getBuffId();
                }
            }
            return null;
        }
    }

    private static String STATE_KEY = "et_phasefieldengine_state";

    private FieldState getState(ShipAPI ship) {
        FieldState state = (FieldState) ship.getCustomData().get(STATE_KEY);
        if (state == null) {
            state = new ReadyState();
            ship.setCustomData(STATE_KEY, state);
        }
        return state;
    }

    private String getStatusBarText() {
        return StringUtils.getString(this.getKey(), "statusBarText");
    }

    private abstract class FieldState extends StateWithNext {
        FieldState() {
            super(STATE_KEY);
        }

        public abstract void advanceAlways(ShipAPI ship);
    }

    private class ReadyState extends FieldState {
        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
            //if phased, set state to phased, set state
            if (ship.getPhaseCloak().getState() == ShipSystemAPI.SystemState.IN
                    || ship.getPhaseCloak().getState() == ShipSystemAPI.SystemState.ACTIVE) {
                setNextState(ship);
            }
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f, RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 0, PhasefieldEngine.this.getStatusBarText(), getTimesPhasedInInterval(ship));
        }

        @Override
        protected float getDuration() {
            return 0f;
        }

        @NotNull
        @Override
        protected StateWithNext getNextState() {
            return new PhasedState();
        }
    }

    private static Color PHASED_STATE_COLOR = new Color(170, 140, 220);

    private class PhasedState extends FieldState {
        @Override
        protected void init(@NotNull ShipAPI ship) {
            if (ship.hasListenerOfClass(ET_PhasefieldEngineListener.class)) {
                ship.removeListenerOfClass(ET_PhasefieldEngineListener.class);
            }

            addToTimesPhased(ship);
            ship.getMutableStats().getPhaseCloakActivationCostBonus().modifyMult(PhasefieldEngine.this.getBuffId(), (float) Math.pow(2, getTimesPhasedInInterval(ship)));
        }

        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
            //if unphased, set state to invuln
            if (ship.getPhaseCloak().getState() == ShipSystemAPI.SystemState.OUT
                    || ship.getPhaseCloak().getState() == ShipSystemAPI.SystemState.COOLDOWN
                    || ship.getPhaseCloak().getState() == ShipSystemAPI.SystemState.IDLE) {
                setNextState(ship);
            }
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f, PHASED_STATE_COLOR, PHASED_STATE_COLOR, 1f, PhasefieldEngine.this.getStatusBarText(), getTimesPhasedInInterval(ship));
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

    private class BuffedState extends FieldState {
        @Override
        protected void init(@NotNull ShipAPI ship) {
            if (!ship.hasListenerOfClass(ET_PhasefieldEngineListener.class)) {
                ET_PhasefieldEngineListener listener = new ET_PhasefieldEngineListener(ship);
                ship.addListener(listener);
            }
        }

        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f - this.getProgressRatio(), RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 1f, PhasefieldEngine.this.getStatusBarText(), getTimesPhasedInInterval(ship));
        }

        @Override
        protected float getDuration() {
            return INVULNERABLE_INTERVAL;
        }

        @Override
        protected boolean intervalExpired(@NotNull ShipAPI ship) {
            setNextState(ship);
            return true;
        }

        @NotNull
        @Override
        protected StateWithNext getNextState() {
            return new CooldownState();
        }
    }

    private class CooldownState extends ReadyState {
        private float endTime;
        @Override
        protected void init(@NotNull ShipAPI ship) {
            endTime = Global.getCombatEngine().getTotalElapsedTime(false) + ship.getPhaseCloak().getCooldown();

            if (ship.hasListenerOfClass(ET_PhasefieldEngineListener.class)) {
                ship.removeListenerOfClass(ET_PhasefieldEngineListener.class);
            }
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            float resetRatio = this.getProgressRatio();

            float remaining = endTime - Global.getCombatEngine().getTotalElapsedTime(false);
            float cooldownRatio = MathUtils.clamp(remaining / ship.getPhaseCloak().getCooldown(), 0f, 1f);

            MagicUI.drawInterfaceStatusBar(ship, 1f - cooldownRatio, RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 1f - resetRatio, PhasefieldEngine.this.getStatusBarText(), getTimesPhasedInInterval(ship));
        }

        @Override
        protected boolean intervalExpired(@NotNull ShipAPI ship) {
            removeTimesPhased(ship);
            ship.getMutableStats().getPhaseCloakActivationCostBonus().unmodifyMult(PhasefieldEngine.this.getBuffId());
            setNextState(ship);
            return true;
        }

        @Override
        protected float getDuration() {
            return PHASE_RESET_INTERVAL;
        }

        @NotNull
        @Override
        protected StateWithNext getNextState() {
            return new ReadyState();
        }
    }
}