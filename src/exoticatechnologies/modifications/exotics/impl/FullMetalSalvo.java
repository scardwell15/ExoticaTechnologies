package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;

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
            maintainStatus(ship,
                    "graphics/icons/hullsys/ammo_feeder.png",
                    StringUtils.getString(this.getKey(), "statusReady"),
                    false);

            if (canSpool(ship)) {
                for (WeaponAPI weapon : ship.getAllWeapons()) {
                    if (weapon.isFiring() && (ship.getShipAI() == null || shouldSpool(weapon))) {
                        interval.setInterval(BUFF_DURATION, BUFF_DURATION);
                        customData.put(getSpooledId(ship), SalvoState.BUFFED);

                        ship.addAfterimage(new Color(255, 0, 0, 150), 0, 0, 0, 0, 0f, 0.1f, 4.6f, 0.25f, true, true, true);
                        break;
                    }
                }
            }
        } else {
            interval.advance(amount);

            if (interval.intervalElapsed()) {
                if (spooled == SalvoState.BUFFED) {
                    interval.setInterval(COOLDOWN, COOLDOWN);
                    customData.put(getSpooledId(ship), SalvoState.RECHARGE);

                    ship.addAfterimage(new Color(0, 100, 255, 150), 0, 0, 0, 0, 0f, 0.1f, 3.3f, 0.25f, true, true, true);

                    ship.getMutableStats().getBallisticWeaponDamageMult().unmodifyMult(this.getBuffId());
                    ship.getMutableStats().getEnergyWeaponDamageMult().unmodifyMult(this.getBuffId());
                    ship.getMutableStats().getProjectileSpeedMult().unmodifyMult(this.getBuffId());
                } else if (spooled == SalvoState.RECHARGE) {
                    customData.put(getSpooledId(ship), SalvoState.READY);
                }
            } else if (spooled == SalvoState.BUFFED) {
                maintainStatus(ship,
                        "graphics/icons/hullsys/ammo_feeder.png",
                        StringUtils.getTranslation(this.getKey(), "statusBuffText")
                                .format("remainingTime", Math.round(interval.getIntervalDuration() - interval.getElapsed()))
                                .toString(),
                        false);

                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult(this.getBuffId(), 1 + DAMAGE_BUFF / 100f);
                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult(this.getBuffId(), 1 + DAMAGE_BUFF / 100f);
                ship.getMutableStats().getProjectileSpeedMult().modifyMult(this.getBuffId(), 1 + DAMAGE_BUFF / 100f);
            } else if (spooled == SalvoState.RECHARGE) {
                maintainStatus(ship,
                        "graphics/icons/hullsys/ammo_feeder.png",
                        StringUtils.getTranslation(this.getKey(), "statusRecharging")
                                .format("remainingTime", Math.round(interval.getIntervalDuration() - interval.getElapsed()))
                                .toString(),
                        false);
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
