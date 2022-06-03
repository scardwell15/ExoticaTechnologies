package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.util.StatUtils;
import lombok.Getter;

import java.awt.*;

public class TracerRecoilCalculator extends Upgrade {
    @Getter protected final float bandwidthUsage = 15f;
    private static float RECOIL_REDUCTION = -20f;
    private static float PROJ_SPEED = -20f;
    private static Color COLOR = new Color(213, 129, 60);

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatPercent(stats.getRecoilPerShotMult(), this.getBuffId(), level, RECOIL_REDUCTION, maxLevel);
        StatUtils.setStatPercent(stats.getMaxRecoilMult(), this.getBuffId(), level, RECOIL_REDUCTION / 2, maxLevel);

        if (level >= 3) {
            StatUtils.setStatPercent(stats.getProjectileSpeedMult(), this.getBuffId(), level - 2, PROJ_SPEED, maxLevel - 2);
        }
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (expand) {
            tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

            this.addDecreaseToTooltip(tooltip,
                    "recoil",
                    fm.getStats().getRecoilPerShotMult().getMultStatMod(this.getBuffId()).getValue());


            MutableStat.StatMod projSpeedStat = fm.getStats().getProjectileSpeedMult().getMultStatMod(this.getBuffId());
            float projSpeedBonus = 1f;
            if (projSpeedStat != null) {
                projSpeedBonus = projSpeedStat.getValue();
            }

            this.addDecreaseToTooltip(tooltip,
                    "projSpeed",
                    projSpeedBonus);
        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }
}
