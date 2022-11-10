package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
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
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
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

    private String getIntervalId(ShipAPI ship) {
        return ship.getId() + this.getKey() + "interval";
    }


    private String getSpooledId(ShipAPI ship) {
        return ship.getId() + this.getKey() + "spooled";
    }

    private boolean shouldSpool(WeaponAPI weapon) {
        if (weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.MISSILE) return false;
        return weapon.hasAIHint(WeaponAPI.AIHints.PD) || weapon.hasAIHint(WeaponAPI.AIHints.PD_ONLY);
    }

    private boolean canSpool(ShipAPI ship) {
        return ship.getShipAI() != null || Mouse.isButtonDown(0);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount, float bandwidth) {
        Map<String, Object> customData = Global.getCombatEngine().getCustomData();
        if (!customData.containsKey(getSpooledId(ship))) {
            customData.put(getIntervalId(ship), new IntervalUtil(COOLDOWN, COOLDOWN));
            customData.put(getSpooledId(ship), SpoolState.SPOOLED);
        }

        SpoolState spooled = (SpoolState) customData.get(getSpooledId(ship));
        IntervalUtil interval = (IntervalUtil) customData.get(getIntervalId(ship));

        if (spooled == SpoolState.SPOOLED) {
            maintainStatus(ship,
                    "graphics/icons/hullsys/ammo_feeder.png",
                    StringUtils.getString(this.getKey(), "statusReady"),
                    false);

            if (canSpool(ship)) {
                for (WeaponAPI weapon : ship.getAllWeapons()) {
                    if (weapon.isFiring() && !(ship.getShipAI() != null && shouldSpool(weapon))) {
                        interval.setInterval(BUFF_DURATION, BUFF_DURATION);
                        customData.put(getSpooledId(ship), SpoolState.BUFFED);

                        ship.addAfterimage(new Color(255, 0, 0, 150), 0, 0, 0, 0, 0f, 0.1f, 4.6f, 0.25f, true, true, true);

                        for (WeaponAPI buffWeapon : ship.getAllWeapons()) {
                            if (buffWeapon.getCooldownRemaining() > (buffWeapon.getCooldown() / 2f)) {
                                buffWeapon.setRemainingCooldownTo(buffWeapon.getCooldown() / 2f);
                            }
                        }

                        break;
                    }
                }
            }
        } else {
            interval.advance(amount);

            if (interval.intervalElapsed()) {
                if (spooled == SpoolState.BUFFED) {
                    interval.setInterval(DEBUFF_DURATION, DEBUFF_DURATION);
                    customData.put(getSpooledId(ship), SpoolState.DEBUFFED);

                    ship.addAfterimage(new Color(0, 100, 255, 150), 0, 0, 0, 0, 0f, 0.1f, 3.3f, 0.25f, true, true, true);
                } else if (spooled == SpoolState.DEBUFFED) {
                    interval.setInterval(COOLDOWN, COOLDOWN);
                    customData.put(getSpooledId(ship), SpoolState.RECHARGE);

                    ship.getMutableStats().getBallisticRoFMult().unmodifyMult(this.getBuffId());
                    ship.getMutableStats().getEnergyRoFMult().unmodifyMult(this.getBuffId());
                } else if (spooled == SpoolState.RECHARGE) {
                    customData.put(getSpooledId(ship), SpoolState.SPOOLED);
                }
            } else if (spooled == SpoolState.BUFFED) {
                maintainStatus(ship,
                        "graphics/icons/hullsys/ammo_feeder.png",
                        StringUtils.getTranslation(this.getKey(), "statusBuffText")
                                .format("remainingTime", Math.round(interval.getIntervalDuration() - interval.getElapsed()))
                                .toString(),
                        false);

                ship.getMutableStats().getBallisticRoFMult().modifyMult(this.getBuffId(), 1 + RATE_OF_FIRE_BUFF / 100f);
                ship.getMutableStats().getEnergyRoFMult().modifyMult(this.getBuffId(), 1 + RATE_OF_FIRE_BUFF / 100f);
            } else if (spooled == SpoolState.DEBUFFED) {
                maintainStatus(ship,
                        "graphics/icons/hullsys/ammo_feeder.png",
                        StringUtils.getString(this.getKey(), "statusDebuffText"),
                        true);

                ship.getMutableStats().getBallisticRoFMult().modifyMult(this.getBuffId(), 1 + RATE_OF_FIRE_DEBUFF / 100f);
                ship.getMutableStats().getEnergyRoFMult().modifyMult(this.getBuffId(), 1 + RATE_OF_FIRE_DEBUFF / 100f);
            } else if (spooled == SpoolState.RECHARGE) {
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

    private enum SpoolState {
        SPOOLED,
        BUFFED,
        DEBUFFED,
        RECHARGE
    }
}
