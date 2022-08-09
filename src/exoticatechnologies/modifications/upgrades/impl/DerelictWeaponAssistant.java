package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.methods.ChipMethod;
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.util.StringUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONException;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class DerelictWeaponAssistant extends Upgrade {
    @Getter protected final float bandwidthUsage = 10f;
    private static final float RECOIL_REDUCTION = -30f;
    private static final float PROJ_SPEED = 20f;
    private static final float FIRERATE_BONUS = 10f;
    private final Set<String> allowedFactions = new HashSet<>();
    private static final Color COLOR = new Color(170, 200, 75);

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public boolean canApply(FleetMemberAPI fm) {
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
    public boolean shouldShow(FleetMemberAPI fm, ShipModifications es, MarketAPI market) {
        if (!canApply(fm)) {
            return false;
        }

        return super.shouldShow(fm, es, market);
    }

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatMult(stats.getRecoilPerShotMultSmallWeaponsOnly(), this.getBuffId(), level, RECOIL_REDUCTION, maxLevel);
        StatUtils.setStatMult(stats.getRecoilPerShotMult(), this.getBuffId(), level, RECOIL_REDUCTION, maxLevel);
        StatUtils.setStatMult(stats.getMaxRecoilMult(), this.getBuffId(), level, RECOIL_REDUCTION, maxLevel);
        StatUtils.setStatPercent(stats.getProjectileSpeedMult(), this.getBuffId(), level, PROJ_SPEED, maxLevel);
        StatUtils.setStatPercent(stats.getBallisticRoFMult(), this.getBuffId(), level, FIRERATE_BONUS, maxLevel);
    }

    @Override
    public void printStatInfoToTooltip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications mods) {
        this.addBenefitToShopTooltipMult(tooltip, "recoil", fm, mods, RECOIL_REDUCTION);
        this.addBenefitToShopTooltip(tooltip, "projSpeed", fm, mods, PROJ_SPEED);
        this.addBenefitToShopTooltip(tooltip, "ballisticFirerate", fm, mods, FIRERATE_BONUS);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (expand) {
            tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

            this.addBenefitToTooltipMult(tooltip,
                    "recoil",
                    fm.getStats().getRecoilPerShotMult().getMultStatMod(this.getBuffId()).getValue());

            this.addBenefitToTooltip(tooltip,
                    "projSpeed",
                    fm.getStats().getProjectileSpeedMult().getPercentStatMod(this.getBuffId()).getValue());

            this.addBenefitToTooltip(tooltip,
                    "ballisticFirerate",
                    fm.getStats().getProjectileSpeedMult().getPercentStatMod(this.getBuffId()).getValue());
        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }
}
