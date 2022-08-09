package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.methods.ChipMethod;
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.json.JSONArray;
import org.json.JSONException;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Log4j
public class AdvancedFluxCoils extends Upgrade {
    @Getter protected final float bandwidthUsage = 30f;
    private static final float CAPACITY_MAX = 25f;
    private static final float VENT_SPEED_MAX = 25f;
    private static final float WEAPON_FLUX_MAX = -10f;
    private final Set<String> allowedFactions = new HashSet<>();
    private static final Color COLOR = new Color(105, 74, 227);

    @Override
    public Color getColor() {
        return COLOR;
    }

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

        if (Misc.isPlayerOrCombinedContainingPlayer(fm.getFleetData().getFleet())) {
            if (Utilities.hasUpgradeChip(fm.getFleetData().getFleet().getCargo(), this.getKey())) {
                return canApply(fm.getVariant());
            }
        }

        return false;
    }

    @Override
    public boolean shouldShow(FleetMemberAPI fm, ShipModifications es, MarketAPI market) {
        if (!canApply(fm)) {
            return false;
        }

        return super.shouldShow(fm, es, market);
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
    public void printStatInfoToTooltip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications mods) {
        this.addBenefitToShopTooltipMult(tooltip, "fluxCapacity", fm, mods, CAPACITY_MAX);
        this.addBenefitToShopTooltip(tooltip, "ventSpeed", fm, mods, VENT_SPEED_MAX);
        this.addBenefitToShopTooltipMult(tooltip, "weaponFluxCosts", fm, mods, WEAPON_FLUX_MAX);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (level > 0) {
            if(expand) {
                tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

                this.addBenefitToTooltipMult(tooltip,
                        "fluxCapacity",
                        (fm.getStats().getFluxCapacity().getMultStatMod(this.getBuffId()).getValue() - 1f) * 100f);

                this.addBenefitToTooltip(tooltip,
                        "ventSpeed",
                        fm.getStats().getVentRateMult().getPercentStatMod(this.getBuffId()).getValue());

                this.addBenefitToTooltipMult(tooltip,
                        "weaponFluxCosts",
                        fm.getStats().getBallisticWeaponFluxCostMod().getMultBonus(this.getBuffId()).getValue());
            } else {
                tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
            }
        }
    }
}
