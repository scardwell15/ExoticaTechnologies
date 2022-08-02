package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;

import java.awt.*;

public class InfernalEngines extends Upgrade {
    @Getter protected final float bandwidthUsage = 10f;
    private static final float SPEED_MULT = 15f;
    private static final float ACCELERATION_MAX = 30f;
    private static final float TURN_RATE_MAX = 30f;
    private static final float BURN_LEVEL_MAX = 20f;

    private static final float DECELERATION_MAX = -30f;
    private static final float FUEL_USE_MAX = 16f;
    private static final Color COLOR = new Color(255, 224, 94);

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public void printStatInfoToTooltip(FleetMemberAPI fm, TooltipMakerAPI tooltip) {
        StringUtils.getTranslation(this.getKey(), "speed")
                .formatWithOneDecimalAndModifier("percent", SPEED_MULT / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", SPEED_MULT)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "accelerationWithFinal")
                .formatWithOneDecimalAndModifier("percent", ACCELERATION_MAX / this.getMaxLevel(fm))
                .formatWithOneDecimalAndModifier("finalValue", ACCELERATION_MAX)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "burnLevel")
                .formatWithOneDecimalAndModifier("percent", BURN_LEVEL_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", BURN_LEVEL_MAX)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "turnrateWithFinal")
                .formatWithOneDecimalAndModifier("percent", TURN_RATE_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", TURN_RATE_MAX)
                .addToTooltip(tooltip);

        StringUtils.getTranslation("ShipListDialog", "UpgradeDrawbackAfterLevel")
                .format("level", 3)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "decelerationWithFinal")
                .formatWithOneDecimalAndModifier("percent", DECELERATION_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", DECELERATION_MAX)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "fuelUse")
                .formatWithOneDecimalAndModifier("percent", FUEL_USE_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", FUEL_USE_MAX)
                .addToTooltip(tooltip);
    }

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



                MutableStat.StatMod decelerationStat = fm.getStats().getDeceleration().getMultStatMod(this.getBuffId());
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
