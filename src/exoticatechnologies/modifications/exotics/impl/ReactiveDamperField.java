package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicUI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.util.RenderUtils;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import exoticatechnologies.util.states.StateWithNext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ReactiveDamperField extends Exotic {
    private static float TRIGGERING_DAMAGE = 100f;
    private static float DAMPER_DURATION = 1f;
    private static float DAMPER_COOLDOWN = 10f;
    private static float DAMPER_REDUCTION = 90f;
    private static float PASSIVE_DAMAGE_TAKEN = 15f;

    @Getter private final Color color = new Color(200,60,20);

    public ReactiveDamperField(String key, JSONObject settings) {
        super(key, settings);
    }

    @Override
    public boolean shouldShow(FleetMemberAPI member, ShipModifications mods, MarketAPI market) {
        return Utilities.hasExoticChip(member.getFleetData().getFleet().getCargo(), this.getKey())
                || Utilities.hasExoticChip(Misc.getStorageCargo(market), this.getKey());
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
                    .format("damperDuration", DAMPER_DURATION)
                    .format("damperReduction", DAMPER_REDUCTION)
                    .format("triggeringDamage", TRIGGERING_DAMAGE)
                    .format("damperCooldown", DAMPER_COOLDOWN)
                    .format("armorDamageTaken", PASSIVE_DAMAGE_TAKEN)
                    .addToTooltip(tooltip, title);
        }
    }

    @Override
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        stats.getArmorDamageTakenMult().modifyPercent(this.getBuffId(), PASSIVE_DAMAGE_TAKEN);
    }

    @Override
    public void advanceInCombatAlways(ShipAPI ship, float bandwidth) {
        DamperState state = getDamperState(ship);
        state.advanceAlways(ship);
    }

    @Override
    public void advanceInCombatUnpaused(ShipAPI ship, float amount, float bandwidth) {
        DamperState state = getDamperState(ship);
        state.advance(ship, amount);
    }

    private static String DAMPER_STATE_ID = "et_damperState";
    private DamperState getDamperState(ShipAPI ship) {
        DamperState state = (DamperState) ship.getCustomData().get(DAMPER_STATE_ID);
        if (state == null) {
            state = new ReadyState();
            ship.setCustomData(DAMPER_STATE_ID, state);
        }
        return state;
    }

    private String getStatusBarText() {
        return StringUtils.getString(this.getKey(), "statusBarText");
    }

    private abstract class DamperState extends StateWithNext {
        public DamperState() {
            super(DAMPER_STATE_ID);
        }

        public void advanceAlways(ShipAPI ship) {

        }
    }

    private class ReadyState extends DamperState {
        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f, RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 0, ReactiveDamperField.this.getStatusBarText(), -1);
        }

        @Override
        protected void advanceShip(ShipAPI ship, float amount) {
            if (!ship.hasListenerOfClass(ET_ReactiveDamperFieldListener.class)) {
                ship.addListener(new ET_ReactiveDamperFieldListener(ship));
            }
        }

        @Override
        protected boolean intervalExpired(ShipAPI ship) {
            ship.addAfterimage(new Color(255, 0, 0, 120), 0, 0, 0, 0, 15f, 0f, 0.1f, 0.75f, true, false, false);
            return false;
        }

        @Override
        protected float getDuration() {
            return 2f;
        }

        @Override
        protected DamperState getNextState() {
            return new ActiveState();
        }
    }

    private class ActiveState extends DamperState {
        @Override
        protected void init(@NotNull ShipAPI ship) {
            ship.addAfterimage(new Color(255, 0, 0, 120), 0, 0, 0, 0, 0f, 0f, 0.75f, 0.33f, true, true, true);
            ship.addAfterimage(new Color(255, 0, 0, 200), 0, 0, 0, 0, 15f, 0f, 0.75f, 0.33f, true, false, false);
        }

        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, 1f - this.getProgressRatio(), RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 0, ReactiveDamperField.this.getStatusBarText(), -1);
        }

        @Override
        protected void advanceShip(ShipAPI ship, float amount) {
        }

        @Override
        protected boolean intervalExpired(@NotNull ShipAPI ship) {
            setNextState(ship);
            return true;
        }

        @Override
        protected float getDuration() {
            return DAMPER_DURATION;
        }

        @Override
        protected DamperState getNextState() {
            return new CooldownState();
        }
    }

    private class CooldownState extends DamperState {
        @Override
        public void advanceAlways(ShipAPI ship) {
            MagicUI.drawInterfaceStatusBar(ship, this.getProgressRatio(), RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(), 0, ReactiveDamperField.this.getStatusBarText(), -1);
        }

        @Override
        protected void advanceShip(ShipAPI ship, float amount) {
        }

        @Override
        protected boolean intervalExpired(@NotNull ShipAPI ship) {
            setNextState(ship);
            return true;
        }

        @Override
        protected float getDuration() {
            return DAMPER_COOLDOWN;
        }

        @Override
        protected DamperState getNextState() {
            return new ReadyState();
        }
    }

    // Our range listener
    @AllArgsConstructor
    private class ET_ReactiveDamperFieldListener implements DamageTakenModifier {
        private final ShipAPI ship;

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (ship.equals(target) && !shieldHit) {
                DamperState state = getDamperState(ship);
                if (state instanceof ReadyState) {
                    float totalDamage = damage.getDamage();
                    if (totalDamage > TRIGGERING_DAMAGE) {
                        state.setNextState(ship);
                        state = getDamperState(ship);
                    }
                }

                if (state instanceof ActiveState) {
                    damage.getModifier().modifyMult(getBuffId(), 1 - DAMPER_REDUCTION);
                    return getBuffId();
                }
            }

            return null;
        }
    }
}