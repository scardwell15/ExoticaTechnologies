package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.methods.ChipMethod;
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

@Log4j
public class AdvancedFluxCoils extends Upgrade {
    @Getter protected final float bandwidthUsage = 30f;
    private static float CAPACITY_MAX = 25f;
    private static float VENT_SPEED_MAX = 25f;
    private static float WEAPON_FLUX_MAX = -10f;
    private Set<String> allowedFactions = new HashSet<>();

    @Override
    public boolean canApply(FleetMemberAPI fm) {
        if (fm.getHullId().contains("ziggurat")) {
            return canApply(fm.getVariant());
        }

        if (fm.getFleetData() == null
                || fm.getFleetData().getFleet() == null
                || allowedFactions.contains(fm.getFleetData().getFleet().getFaction().toString())) {
            return canApply(fm.getVariant());
        }

        if (fm.getFleetData() != null && fm.getFleetData().getFleet() != null) {
            if (Utilities.hasUpgradeChip(fm.getFleetData().getFleet().getCargo(), this.getKey())) {
                return canApply(fm.getVariant());
            }
        }

        return false;
    }

    @Override
    protected void loadConfig() throws JSONException {
        JSONArray allowedFactions = this.upgradeSettings.getJSONArray("allowedFactions");
        for (int i = 0; i < allowedFactions.length(); i++) {
            this.allowedFactions.add(allowedFactions.getString(i));
        }
    }

    @Override
    public boolean canUseUpgradeMethod(FleetMemberAPI fm, ShipModifications mods, UpgradeMethod method) {
        if (mods.hasUpgrade(this)) {
            return true;
        }
        return method instanceof ChipMethod;
    }

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatMult(stats.getFluxCapacity(), this.getBuffId(), level, CAPACITY_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getVentRateMult(), this.getBuffId(), level, VENT_SPEED_MAX, maxLevel);
        StatUtils.setStatMult(stats.getBallisticWeaponFluxCostMod(), this.getBuffId(), level, WEAPON_FLUX_MAX, maxLevel);
        StatUtils.setStatMult(stats.getEnergyWeaponFluxCostMod(), this.getBuffId(), level, WEAPON_FLUX_MAX, maxLevel);
        StatUtils.setStatMult(stats.getMissileWeaponFluxCostMod(), this.getBuffId(), level, WEAPON_FLUX_MAX, maxLevel);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (level > 0) {
            if(expand) {
                tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

                this.addIncreaseToTooltip(tooltip,
                        "fluxCapacity",
                        (fm.getStats().getFluxCapacity().getMultStatMod(this.getBuffId()).getValue() - 1f) * 100f);

                this.addIncreaseToTooltip(tooltip,
                        "ventSpeed",
                        fm.getStats().getVentRateMult().getPercentStatMod(this.getBuffId()).getValue());

                this.addDecreaseToTooltip(tooltip,
                        "weaponFluxCosts",
                        fm.getStats().getBallisticWeaponFluxCostMod().getMultBonus(this.getBuffId()).getValue());
            } else {
                tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
            }
        }
    }
}
