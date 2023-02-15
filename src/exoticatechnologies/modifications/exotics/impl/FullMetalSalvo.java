package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.combat.entities.Missile;
import data.scripts.util.MagicUI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticData;
import exoticatechnologies.util.RenderUtils;
import exoticatechnologies.util.reflect.FieldWrapperKT;
import exoticatechnologies.util.reflect.ReflectionUtil;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.states.StateWithNext;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

public class FullMetalSalvo extends Exotic {

    private static float DAMAGE_BUFF = 100f;
    private static float RATE_OF_FIRE_DEBUFF = -33f;

    private static int COOLDOWN = 8;
    private static int BUFF_DURATION = 2;

    @Getter
    private final Color color = new Color(0xD99836);

    public FullMetalSalvo(String key, JSONObject settings) {
        super(key, settings);
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
                    .format("damageBoost", DAMAGE_BUFF)
                    .format("boostTime", BUFF_DURATION)
                    .format("cooldown", COOLDOWN)
                    .format("firerateMalus", Math.abs(RATE_OF_FIRE_DEBUFF))
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public void applyExoticToStats(String id, FleetMemberAPI fm, MutableShipStatsAPI stats, ExoticData data) {
        stats.getBallisticRoFMult().modifyMult(this.getBuffId(), 1 + RATE_OF_FIRE_DEBUFF / 100f);
        stats.getEnergyRoFMult().modifyMult(this.getBuffId(), 1 + RATE_OF_FIRE_DEBUFF / 100f);
    }

    private static boolean shouldSpoolAI(WeaponAPI weapon) {
        if (weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.MISSILE) return false;
        return !weapon.hasAIHint(WeaponAPI.AIHints.PD) && !weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY);
    }

    private static boolean canSpool(ShipAPI ship) {
        return ship.getShipAI() != null || Mouse.isButtonDown(0);
    }

    @Override
    public void advanceInCombatAlways(ShipAPI ship, ExoticData data) {
        SalvoState state = getSalvoState(ship);
        state.advanceAlways(ship);
    }

    @Override
    public void advanceInCombatUnpaused(ShipAPI ship, float amount, ExoticData data) {
        SalvoState state = getSalvoState(ship);

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        state.advance(ship, amount);
    }

    public void gigaProjectiles(ShipAPI source) {
        for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {
            if (proj.getSource().equals(source) && proj.getElapsed() <= Global.getCombatEngine().getElapsedInLastFrame()) {
                proj.getDamage().getModifier().modifyMult(this.getBuffId(), 1 + DAMAGE_BUFF / 100f);

                if (proj instanceof Missile) {
                    Missile missile = (Missile) proj;

                    try {
                        FieldWrapperKT<Float> missileSpeed = ReflectionUtil.getObjectFieldWrapper(missile, "maxSpeed", float.class);
                        missileSpeed.setValue(missileSpeed.getValue() * (1 + DAMAGE_BUFF / 100f));

                        FieldWrapperKT<Object> engineStatsField = ReflectionUtil.getObjectFieldWrapper(missile, "engineStats", Object.class);
                        Object engineStats = engineStatsField.getValue();

                        MutableStat speedStat = (MutableStat) MethodHandles.lookup().findVirtual(engineStats.getClass(), "getMaxSpeed", MethodType.methodType(MutableStat.class)).invoke(engineStats);
                        speedStat.modifyMult(getBuffId(), 1 + DAMAGE_BUFF / 100f);

                        MutableStat accelStat = (MutableStat) MethodHandles.lookup().findVirtual(engineStats.getClass(), "getAcceleration", MethodType.methodType(MutableStat.class)).invoke(engineStats);
                        accelStat.modifyMult(getBuffId(), 1 + DAMAGE_BUFF / 100f);
                    } catch (Throwable ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    proj.getVelocity().scale(1 + DAMAGE_BUFF / 100f);
                }
            }
        }
    }

    private static String STATE_KEY = "et_salvo_state";
    private SalvoState getSalvoState(ShipAPI ship) {
        SalvoState state = (SalvoState) ship.getCustomData().get(STATE_KEY);
        if (state == null) {
            state = new ReadyState();
            ship.setCustomData(STATE_KEY, state);
        }
        return state;
    }

    private String getSalvoStatusBarText() {
        return StringUtils.getString(this.getKey(), "statusBarText");
    }

    private abstract class SalvoState extends StateWithNext {
        SalvoState() {
            super(STATE_KEY);
        }

        public abstract void advanceAlways(ShipAPI ship);
    }

    boolean run = false;
    private class ReadyState extends SalvoState {
        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
            if (canSpool(ship)) {
                for (WeaponAPI weapon : ship.getAllWeapons()) {
                    if (weapon.isFiring() && (ship.getShipAI() == null || shouldSpoolAI(weapon))) {
                        setNextState(ship);
                        ship.addAfterimage(new Color(255, 0, 0, 150), 0, 0, 0, 0, 0f, 0.1f, 1.75f, 0.25f, true, true, true);
                        break;
                    }
                }
            }
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f, RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 0, FullMetalSalvo.this.getSalvoStatusBarText(), -1);
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

    private class BuffedState extends SalvoState {
        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
            gigaProjectiles(ship);
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f - this.getProgressRatio(), RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 0, FullMetalSalvo.this.getSalvoStatusBarText(), -1);
        }

        @Override
        protected float getDuration() {
            return BUFF_DURATION;
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

    private class CooldownState extends SalvoState {
        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            float ratio = this.getProgressRatio();
            Color progressBarColor = RenderUtils.mergeColors(RenderUtils.getEnemyUIColor(), RenderUtils.getAliveUIColor(), ratio);

            MagicUI.drawInterfaceStatusBar(ship, ratio, progressBarColor, RenderUtils.getAliveUIColor(), 0, FullMetalSalvo.this.getSalvoStatusBarText(), -1);
        }

        @Override
        protected boolean intervalExpired(@NotNull ShipAPI ship) {
            setNextState(ship);
            return true;
        }

        @Override
        protected float getDuration() {
            return COOLDOWN;
        }

        @NotNull
        @Override
        protected StateWithNext getNextState() {
            return new ReadyState();
        }
    }
}
