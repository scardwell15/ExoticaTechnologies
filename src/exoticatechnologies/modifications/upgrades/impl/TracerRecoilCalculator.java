package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.util.StatUtils;
import lombok.Getter;

public class TracerRecoilCalculator extends Upgrade {
    @Getter protected final float bandwidthUsage = 15f;
    private static float RECOIL_REDUCTION = -30f;

    private static float PROJ_SPEED = -20f;

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatPercent(stats.getRecoilPerShotMult(), this.getBuffId(), level, RECOIL_REDUCTION, maxLevel);
        StatUtils.setStatPercent(stats.getMaxRecoilMult(), this.getBuffId(), level, RECOIL_REDUCTION / 2, maxLevel);

        StatUtils.setStatPercent(stats.getProjectileSpeedMult(), this.getBuffId(), level, PROJ_SPEED, maxLevel);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (expand) {
            tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

            this.addIncreaseToTooltip(tooltip,
                    "recoil",
                    fm.getStats().getRecoilPerShotMult().getMultStatMod(this.getBuffId()).getValue());

            this.addIncreaseToTooltip(tooltip,
                    "projSpeed",
                    fm.getStats().getProjectileSpeedMult().getMultStatMod(this.getBuffId()).getValue());
        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }
}
