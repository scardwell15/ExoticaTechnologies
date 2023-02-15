package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import data.scripts.util.MagicUI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticData;
import exoticatechnologies.util.RenderUtils;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import exoticatechnologies.util.states.StateWithNext;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SpooledFeeders extends Exotic {
    private static final String ITEM = "et_ammospool";

    private static float RATE_OF_FIRE_BUFF = 100f;
    private static float RATE_OF_FIRE_DEBUFF = -33f;

    private static int COOLDOWN = 16;
    private static int BUFF_DURATION = 5;
    private static int DEBUFF_DURATION = 4;

    @Getter
    private final Color color = new Color(0xD93636);

    public SpooledFeeders(String key, JSONObject settings) {
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
                    .format("firerateBoost", RATE_OF_FIRE_BUFF)
                    .format("boostTime", BUFF_DURATION)
                    .format("firerateMalus", Math.abs(RATE_OF_FIRE_DEBUFF))
                    .format("malusTime", DEBUFF_DURATION)
                    .format("cooldownTime", COOLDOWN)
                    .addToTooltip(tooltip, title);
        }
    }

    private boolean shouldSpoolAI(WeaponAPI weapon) {
        if (weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.MISSILE) return false;
        return weapon.hasAIHint(WeaponAPI.AIHints.PD) || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY);
    }

    private boolean canSpool(ShipAPI ship) {
        return ship.getShipAI() != null || Mouse.isButtonDown(0);
    }

    @Override
    public void advanceInCombatAlways(ShipAPI ship, ExoticData data) {
        SpoolState state = getSpoolState(ship);
        state.advanceAlways(ship);
    }

    @Override
    public void advanceInCombatUnpaused(ShipAPI ship, float amount, ExoticData data) {
        SpoolState state = getSpoolState(ship);
        state.advance(ship, amount);
    }

    private static String STATE_KEY = "et_spool_state";

    private SpoolState getSpoolState(ShipAPI ship) {
        SpoolState state = (SpoolState) ship.getCustomData().get(STATE_KEY);
        if (state == null) {
            state = new ReadyState();
            ship.setCustomData(STATE_KEY, state);
        }
        return state;
    }

    private String getStatusBarText() {
        return StringUtils.getString(this.getKey(), "statusBarText");
    }

    private abstract class SpoolState extends StateWithNext {
        SpoolState() {
            super(STATE_KEY);
        }

        public abstract void advanceAlways(ShipAPI ship);
    }

    private class ReadyState extends SpoolState {
        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
            if (canSpool(ship)) {
                for (WeaponAPI weapon : ship.getAllWeapons()) {
                    if (weapon.isFiring() && (ship.getShipAI() == null || shouldSpoolAI(weapon))) {
                        setNextState(ship);
                        break;
                    }
                }
            }
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f, RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 0, SpooledFeeders.this.getStatusBarText(), -1);
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

    private class BuffedState extends SpoolState {
        @Override
        protected void init(@NotNull ShipAPI ship) {
            ship.addAfterimage(new Color(255, 0, 0, 150), 0, 0, 0, 0, 0f, 0.1f, 4.6f, 0.25f, true, true, true);

            ship.getMutableStats().getBallisticRoFMult().modifyMult(SpooledFeeders.this.getBuffId(), 1 + RATE_OF_FIRE_BUFF / 100f);
            ship.getMutableStats().getEnergyRoFMult().modifyMult(SpooledFeeders.this.getBuffId(), 1 + RATE_OF_FIRE_BUFF / 100f);

            for (WeaponAPI buffWeapon : ship.getAllWeapons()) {
                if (buffWeapon.getCooldownRemaining() > (buffWeapon.getCooldown() / 2f)) {
                    buffWeapon.setRemainingCooldownTo(buffWeapon.getCooldown() / 2f);
                }
            }
        }

        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f - this.getProgressRatio(), RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 0, SpooledFeeders.this.getStatusBarText(), -1);
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

    private class CooldownState extends SpoolState {
        @Override
        protected void init(@NotNull ShipAPI ship) {
            ship.getMutableStats().getBallisticRoFMult().modifyMult(SpooledFeeders.this.getBuffId(), 1 + RATE_OF_FIRE_DEBUFF / 100f);
            ship.getMutableStats().getEnergyRoFMult().modifyMult(SpooledFeeders.this.getBuffId(), 1 + RATE_OF_FIRE_DEBUFF / 100f);
        }

        @Override
        protected void advanceShip(@NotNull ShipAPI ship, float amount) {
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            float ratio = this.getProgressRatio();
            Color progressBarColor = RenderUtils.mergeColors(RenderUtils.getEnemyUIColor(), RenderUtils.getAliveUIColor(), ratio);

            MagicUI.drawInterfaceStatusBar(ship, ratio, progressBarColor, RenderUtils.getAliveUIColor(), 0, SpooledFeeders.this.getStatusBarText(), -1);
        }

        @Override
        protected boolean intervalExpired(@NotNull ShipAPI ship) {
            setNextState(ship);
            ship.getMutableStats().getBallisticRoFMult().unmodifyMult(SpooledFeeders.this.getBuffId());
            ship.getMutableStats().getEnergyRoFMult().unmodifyMult(SpooledFeeders.this.getBuffId());
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
