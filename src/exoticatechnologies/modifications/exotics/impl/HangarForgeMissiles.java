package exoticatechnologies.modifications.exotics.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import exoticatechnologies.campaign.rulecmd.ETInteractionDialogPlugin;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.hullmods.ExoticaTechHM;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import org.json.JSONException;

import java.awt.*;
import java.util.Map;

public class HangarForgeMissiles extends Exotic {
    private static final String ITEM = "et_hangarforge";
    private static final Color[] tooltipColors = {new Color(0xFF8902), ExoticaTechHM.infoColor, ExoticaTechHM.infoColor};

    private static float COST_CREDITS = 150000;
    private static int SECONDS_PER_RELOAD = 90;
    private static float PERCENT_RELOADED = 50f;

    @Getter private final Color mainColor = new Color(0xFF8902);

    @Override
    public void loadConfig() throws JSONException {
        SECONDS_PER_RELOAD = (int) exoticSettings.getInt("secondsBetweenReloads");
        PERCENT_RELOADED = (float) exoticSettings.getDouble("percentReloaded");
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItem(fleet.getCargo(), ITEM)
                && fleet.getCargo().getCredits().get() >= COST_CREDITS;
    }

    @Override
    public String getUnableToApplyTooltip(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        if (fleet.getCargo().getCredits().get() < COST_CREDITS) {
            return StringUtils.getTranslation(this.getKey(), "needCredits")
                    .format("needCredits", COST_CREDITS)
                    .toString();
        }

        return StringUtils.getTranslation(this.getKey(), "needItem")
                .format("itemName", Global.getSettings().getSpecialItemSpec(ITEM).getName())
                .toString();
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        fleet.getCargo().getCredits().subtract(150000);
        Utilities.takeItemQuantity(fleet.getCargo(), ITEM, 1);
        return true;
    }

    @Override
    public boolean restoreItemsToFleet(CampaignFleetAPI fleet, FleetMemberAPI fm) {
        Utilities.addItem(fleet, ITEM, 1);
        return true;
    }

    @Override
    public void modifyResourcesPanel(InteractionDialogAPI dialog, ETInteractionDialogPlugin plugin, Map<String, Float> resourceCosts, FleetMemberAPI fm) {
        resourceCosts.put(ITEM, 1f);
        resourceCosts.put(Commodities.CREDITS, 150000f);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        if (systems.hasExotic(this.getKey())) {
            if (expand) {
                StringUtils.getTranslation(this.getKey(), "longDescription")
                        .format("exoticName", this.getName())
                        .format("reloadSize", PERCENT_RELOADED)
                        .format("reloadTime", SECONDS_PER_RELOAD)
                        .addToTooltip(tooltip, tooltipColors);
            } else {
                tooltip.addPara(this.getName(), tooltipColors[0], 5);
            }
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
    public void advanceInCombat(ShipAPI ship, float amount, float bandwidth) {
        IntervalUtil reloadInterval = getReloadInterval(ship);
        if (reloadInterval == null) {
            reloadInterval = createReloadInterval(ship);
        }

        reloadInterval.advance(amount);

        if(reloadInterval.intervalElapsed()) {

            boolean addedAmmo = false;
            for(WeaponAPI weapon : ship.getAllWeapons()) {
                if(weapon.getAmmoTracker() != null && weapon.getAmmoTracker().usesAmmo() && weapon.getAmmoTracker().getAmmoPerSecond() == 0f) {

                    int ammo = weapon.getAmmoTracker().getAmmo();
                    int maxAmmo = weapon.getAmmoTracker().getMaxAmmo();

                    if(ammo < maxAmmo) {
                        weapon.getAmmoTracker().setAmmo((int) Math.min(maxAmmo, ammo + maxAmmo * PERCENT_RELOADED / 100f));
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
}