package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.modifications.upgrades.Upgrade;
import lombok.Getter;

public class InfernalEngines extends Upgrade {
    @Getter protected final float bandwidthUsage = 10f;
    private static float SPEED_MULT = 15f;
    private static float ACCELERATION_MAX = 30f;
    private static float TURN_RATE_MAX = 30f;
    private static float BURN_LEVEL_MAX = 20f;

    private static float DECELERATION_MAX = -30f;
    private static float FUEL_USE_MAX = 16f;

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatPercent(stats.getMaxSpeed(), this.getBuffId(), level, SPEED_MULT, maxLevel);

        StatUtils.setStatPercent(stats.getAcceleration(), this.getBuffId(), level, ACCELERATION_MAX, maxLevel);

        StatUtils.setStatPercent(stats.getMaxTurnRate(), this.getBuffId(), level, TURN_RATE_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getTurnAcceleration(), this.getBuffId(), level, TURN_RATE_MAX, maxLevel);

        StatUtils.setStatPercent(stats.getMaxBurnLevel(), this.getBuffId(), level, BURN_LEVEL_MAX, maxLevel);


        if (level >= 3) {
            StatUtils.setStatMult(stats.getDeceleration(), this.getBuffId(), level - 2, DECELERATION_MAX, maxLevel - 2);
            StatUtils.setStatPercent(stats.getFuelUseMod(), this.getBuffId(), level - 2, FUEL_USE_MAX, maxLevel - 2);
        }
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (level > 0) {
            if(expand) {
                tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

                this.addIncreaseWithFinalToTooltip(tooltip,
                        "speed",
                        fm.getStats().getMaxSpeed().getPercentStatMod(this.getBuffId()).getValue(),
                        fm.getStats().getMaxSpeed().getBaseValue());

                this.addIncreaseToTooltip(tooltip,
                        "acceleration",
                        fm.getStats().getAcceleration().getPercentStatMod(this.getBuffId()).getValue());

                this.addIncreaseToTooltip(tooltip,
                        "turnrate",
                        fm.getStats().getMaxTurnRate().getPercentStatMod(this.getBuffId()).getValue());

                this.addIncreaseWithFinalToTooltip(tooltip,
                        "burnLevel",
                        fm.getStats().getMaxBurnLevel().getPercentStatMod(this.getBuffId()).getValue(),
                        fm.getStats().getMaxBurnLevel().getBaseValue());



                MutableStat.StatMod decelerationStat = fm.getStats().getDeceleration().getPercentStatMod(this.getBuffId());
                float decelerationBonus = 1f;
                if (decelerationStat != null) {
                    decelerationBonus = decelerationStat.getValue();
                }

                this.addDecreaseToTooltip(tooltip,
                        "deceleration",
                        decelerationBonus);

                MutableStat.StatMod fuelUseStat = fm.getStats().getFuelUseMod().getPercentBonus(this.getBuffId());
                float fuelUseBonus = 0f;
                if (fuelUseStat != null) {
                    fuelUseBonus = fuelUseStat.getValue();
                }

                this.addIncreaseWithFinalToTooltip(tooltip,
                        "fuelUse",
                        fuelUseBonus,
                        fm.getHullSpec().getFuelPerLY());
            } else {
                tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
            }
        }
    }
}
