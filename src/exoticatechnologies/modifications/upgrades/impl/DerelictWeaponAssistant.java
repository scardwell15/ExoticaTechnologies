package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.methods.ChipMethod;
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.util.Utilities;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONException;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class DerelictWeaponAssistant extends Upgrade {
    @Getter protected final float bandwidthUsage = 10f;
    private static float RECOIL_REDUCTION = -25f;
    private static float PROJ_SPEED = 20f;
    private static float FIRERATE_BONUS = 10f;
    private Set<String> allowedFactions = new HashSet<>();
    private static Color COLOR = new Color(170, 200, 75);

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
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatPercent(stats.getRecoilPerShotMult(), this.getBuffId(), level, RECOIL_REDUCTION, maxLevel);
        StatUtils.setStatPercent(stats.getMaxRecoilMult(), this.getBuffId(), level, RECOIL_REDUCTION / 2, maxLevel);
        StatUtils.setStatPercent(stats.getProjectileSpeedMult(), this.getBuffId(), level, PROJ_SPEED, maxLevel);
        StatUtils.setStatPercent(stats.getBallisticRoFMult(), this.getBuffId(), level, FIRERATE_BONUS, maxLevel);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (expand) {
            tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

            this.addDecreaseToTooltip(tooltip,
                    "recoil",
                    fm.getStats().getRecoilPerShotMult().getMultStatMod(this.getBuffId()).getValue());

            this.addIncreaseToTooltip(tooltip,
                    "projSpeed",
                    fm.getStats().getProjectileSpeedMult().getPercentStatMod(this.getBuffId()).getValue());

            this.addIncreaseToTooltip(tooltip,
                    "ballisticFirerate",
                    fm.getStats().getProjectileSpeedMult().getPercentStatMod(this.getBuffId()).getValue());
        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }
}
