package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicTargeting;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
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
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
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
    public void applyExoticToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, float bandwidth, String id) {
        stats.getBallisticRoFMult().modifyMult(this.getBuffId(), 1 + RATE_OF_FIRE_DEBUFF / 100f);
        stats.getEnergyRoFMult().modifyMult(this.getBuffId(), 1 + RATE_OF_FIRE_DEBUFF / 100f);
    }

    private String getIntervalId(ShipAPI ship) {
        return ship.getId() + this.getKey() + "interval";
    }


    private String getSpooledId(ShipAPI ship) {
        return ship.getId() + this.getKey() + "spooled";
    }

    private boolean shouldSpool(WeaponAPI weapon) {
        if (weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.MISSILE) return false;
        return !weapon.hasAIHint(WeaponAPI.AIHints.PD) && !weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY);
    }

    private boolean canSpool(ShipAPI ship) {
        return ship.getShipAI() != null || Mouse.isButtonDown(0);
    }


    @Override
    public void advanceInCombat(ShipAPI ship, float amount, float bandwidth) {
        Map<String, Object> customData = Global.getCombatEngine().getCustomData();
        if (!customData.containsKey(getSpooledId(ship))) {
            customData.put(getIntervalId(ship), new IntervalUtil(COOLDOWN, COOLDOWN));
            customData.put(getSpooledId(ship), SalvoState.READY);
        }

        SalvoState spooled = (SalvoState) customData.get(getSpooledId(ship));
        IntervalUtil interval = (IntervalUtil) customData.get(getIntervalId(ship));

        if (spooled == SalvoState.READY) {
            ready(ship, interval);
        } else {
            interval.advance(amount);

            if (spooled == SalvoState.BUFFED) {
                buffed(ship, interval);
            } else if (spooled == SalvoState.RECHARGE) {
                recharge(ship, interval);
            }
        }
    }

    public void ready(ShipAPI ship, IntervalUtil interval) {
        Map<String, Object> customData = Global.getCombatEngine().getCustomData();

        maintainStatus(ship,
                "graphics/icons/hullsys/ammo_feeder.png",
                StringUtils.getString(this.getKey(), "statusReady"),
                false);

        if (canSpool(ship)) {
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.isFiring() && (ship.getShipAI() == null || shouldSpool(weapon))) {
                    interval.setInterval(BUFF_DURATION, BUFF_DURATION);
                    customData.put(getSpooledId(ship), SalvoState.BUFFED);

                    ship.addAfterimage(new Color(255, 0, 0, 150), 0, 0, 0, 0, 0f, 0.1f, 1.75f, 0.25f, true, true, true);
                    break;
                }
            }
        }
    }

    public void buffed(ShipAPI ship, IntervalUtil interval) {
        Map<String, Object> customData = Global.getCombatEngine().getCustomData();
        if (interval.intervalElapsed()) {
            interval.setInterval(COOLDOWN, COOLDOWN);
            customData.put(getSpooledId(ship), SalvoState.RECHARGE);

            ship.getMutableStats().getProjectileSpeedMult().unmodifyMult(this.getBuffId());
        } else {
            gigaProjectiles(ship);

            maintainStatus(ship,
                    "graphics/icons/hullsys/ammo_feeder.png",
                    StringUtils.getTranslation(this.getKey(), "statusBuffText")
                            .format("remainingTime", Math.round(interval.getIntervalDuration() - interval.getElapsed()))
                            .toString(),
                    false);
        }
    }

    public void recharge(ShipAPI ship, IntervalUtil interval) {

        Map<String, Object> customData = Global.getCombatEngine().getCustomData();
        if (interval.intervalElapsed()) {
            customData.put(getSpooledId(ship), SalvoState.READY);
        } else {
            maintainStatus(ship,
                    "graphics/icons/hullsys/ammo_feeder.png",
                    StringUtils.getTranslation(this.getKey(), "statusRecharging")
                            .format("remainingTime", Math.round(interval.getIntervalDuration() - interval.getElapsed()))
                            .toString(),
                    false);
        }
    }

    public void gigaProjectiles(ShipAPI source) {
        source.getMutableStats().getProjectileSpeedMult().modifyMult(this.getBuffId(), 1 + DAMAGE_BUFF / 100f);

        for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {
            if (proj.getSource().equals(source) && proj.getElapsed() <= Global.getCombatEngine().getElapsedInLastFrame()) {
                proj.getDamage().getModifier().modifyMult(this.getBuffId(), 1 + DAMAGE_BUFF / 100f);
                proj.getVelocity().scale(1 + DAMAGE_BUFF / 100f);
            }
        }
    }

    public static boolean isPlayerShip(ShipAPI ship) {
        return Global.getCombatEngine().getPlayerShip() != null
                && Global.getCombatEngine().getPlayerShip().equals(ship);
    }

    public void maintainStatus(ShipAPI ship, String spriteName, String translation, boolean isDebuff) {
        if (isPlayerShip(ship)) {
            Global.getCombatEngine().maintainStatusForPlayerShip(
                    this.getBuffId(),
                    "graphics/icons/hullsys/ammo_feeder.png",
                    StringUtils.getString(this.getKey(), "statusTitle"),
                    translation,
                    false);
        }
    }

    private enum SalvoState {
        READY,
        BUFFED,
        RECHARGE
    }
}
