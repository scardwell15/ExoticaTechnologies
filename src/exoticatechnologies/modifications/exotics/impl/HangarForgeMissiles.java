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
import data.scripts.util.MagicUI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.util.RenderUtils;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HangarForgeMissiles extends Exotic {
    private static final String ITEM = "et_hangarforge";

    private static final float COST_CREDITS = 150000;
    private static int SECONDS_PER_RELOAD = 60;
    private static float PERCENT_RELOADED = 50f;

    private static final Set<String> blacklistedWeapons = new HashSet<>();

    @Getter private final Color color = new Color(0xFF8902);

    public HangarForgeMissiles(String key, JSONObject settings) {
        super(key, settings);
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM)
                && fleet.getCargo().getCredits().get() >= COST_CREDITS;
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm, MarketAPI market) {
        fleet.getCargo().getCredits().subtract(150000);
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
                    .format("reloadSize", PERCENT_RELOADED)
                    .format("reloadTime", SECONDS_PER_RELOAD)
                    .addToTooltip(tooltip, title);
        }
    }

    private String getReloadId(ShipAPI ship) {
        return String.format("%s%s_reload", this.getBuffId(), ship.getId());
    }

    private IntervalUtil getReloadInterval(ShipAPI ship) {
        Object val = Global.getCombatEngine().getCustomData().get(getReloadId(ship));
        if (val != null) {
            return (IntervalUtil) val;
        }
        return null;
    }

    private IntervalUtil createReloadInterval(ShipAPI ship) {
        IntervalUtil interval = new IntervalUtil(SECONDS_PER_RELOAD, SECONDS_PER_RELOAD);
        Global.getCombatEngine().getCustomData().put(getReloadId(ship), interval);
        return interval;
    }

    @Override
    public void advanceInCombatAlways(ShipAPI ship, float bandwidth) {
        IntervalUtil reloadInterval = getReloadInterval(ship);

        if (reloadInterval != null) {
            MagicUI.drawInterfaceStatusBar(ship, reloadInterval.getElapsed() / reloadInterval.getIntervalDuration(), RenderUtils.getAliveUIColor(), RenderUtils.getAliveUIColor(),
                    0f, StringUtils.getString(this.getKey(), "statusBarText"), -1);
        }
    }

    @Override
    public void advanceInCombatUnpaused(ShipAPI ship, float amount, float bandwidth) {
        IntervalUtil reloadInterval = getReloadInterval(ship);

        if (reloadInterval == null) {
            reloadInterval = createReloadInterval(ship);

            //reduce ammo for weapons
            for(WeaponAPI weapon : ship.getAllWeapons()) {

                if (blacklistedWeapons.contains(weapon.getId())) {
                    continue;
                }

                if(shouldAffectWeapon(weapon)) {
                    int wepMaxAmmo = weapon.getMaxAmmo();

                    int newMaxAmmo = (int) Math.ceil(wepMaxAmmo * PERCENT_RELOADED / 100f);
                    if (weapon.getSpec().getBurstSize() > 0) {
                        newMaxAmmo = Math.max(newMaxAmmo, weapon.getSpec().getBurstSize());
                    }

                    weapon.setMaxAmmo(newMaxAmmo);
                    weapon.getAmmoTracker().setMaxAmmo(newMaxAmmo);

                    int newAmmo = Math.min(newMaxAmmo, weapon.getAmmoTracker().getAmmo());
                    weapon.setAmmo(newAmmo);
                    weapon.getAmmoTracker().setAmmo(newAmmo);
                }
            }
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        reloadInterval.advance(amount);

        if(reloadInterval.intervalElapsed()) {
            boolean addedAmmo = false;
            for(WeaponAPI weapon : ship.getAllWeapons()) {
                if(shouldAffectWeapon(weapon)) {
                    int ammo = weapon.getAmmoTracker().getAmmo();
                    int maxAmmo = weapon.getAmmoTracker().getMaxAmmo();

                    if(ammo < maxAmmo) {
                        int ammoToReload = ammo + (int) Math.ceil(maxAmmo * PERCENT_RELOADED / 100f);
                        weapon.getAmmoTracker().setAmmo((int) Math.min(maxAmmo, ammoToReload));
                        addedAmmo = true;
                    }
                }
            }

            if (addedAmmo) {
                reloadInterval.setInterval(SECONDS_PER_RELOAD, SECONDS_PER_RELOAD);

                Global.getCombatEngine().addFloatingText(
                        ship.getLocation(),
                        StringUtils.getString(this.getKey(), "statusReloaded"),
                        8,
                        Color.WHITE,
                        ship,
                        2,
                        2
                );
            } else {
                reloadInterval.setInterval(10f, 10f);
            }
        }
    }

    private boolean shouldAffectWeapon(WeaponAPI weapon) {
        return weapon.getAmmoTracker() != null
                && weapon.getAmmoTracker().usesAmmo()
                && weapon.getAmmoTracker().getMaxAmmo() > 1
                && weapon.getAmmoTracker().getAmmoPerSecond() == 0f
                && weapon.getType().equals(WeaponAPI.WeaponType.MISSILE);
    }
}