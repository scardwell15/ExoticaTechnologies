package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;

import java.awt.*;

public class ForcedOvertime extends Upgrade {
    @Getter protected final float bandwidthUsage = 5f;
    private static final float CR_TO_DEPLOY_MAX = -20f;
    private static final float CR_RECOVERY_RATE_MAX = -20f;

    private static final float REQUIRED_CREW_MAX = 20f;
    private static final float PEAK_CR_MAX = 20f;
    private static final float CR_LOSS_MAX = 20f;

    private static final float FRIGATE_MULT = 8f;
    private static final float DESTROYER_MULT = 3f;
    private static final float CRUISER_MULT = 1.5f;
    private static final float CAPITAL_MULT = 1f;
    private static final Color COLOR = new Color(124, 124, 124);

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public void printStatInfoToTooltip(FleetMemberAPI fm, TooltipMakerAPI tooltip) {
        float delta;
        if (fm.getHullSpec().getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
            delta = CAPITAL_MULT;
        } else if (fm.getHullSpec().getHullSize() == ShipAPI.HullSize.CRUISER) {
            delta = CRUISER_MULT;
        } else if (fm.getHullSpec().getHullSize() == ShipAPI.HullSize.DESTROYER) {
            delta = DESTROYER_MULT;
        } else {
            delta = FRIGATE_MULT;
        }

        StringUtils.getTranslation(this.getKey(), "peakPerformanceTime")
                .formatWithOneDecimalAndModifier("percent", PEAK_CR_MAX / this.getMaxLevel(fm) * delta)
                .formatPercWithOneDecimalAndModifier("finalValue", PEAK_CR_MAX * delta)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "crPerDeployment")
                .formatWithOneDecimalAndModifier("percent", CR_TO_DEPLOY_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", CR_TO_DEPLOY_MAX)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "requiredCrew")
                .formatWithOneDecimalAndModifier("percent", REQUIRED_CREW_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", REQUIRED_CREW_MAX)
                .addToTooltip(tooltip);

        //after level 3
        StringUtils.getTranslation("ShipListDialog", "UpgradeDrawbackAfterLevel")
                .format("level", 3)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "crRecoveryRate")
                .formatWithOneDecimalAndModifier("percent", CR_RECOVERY_RATE_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", CR_RECOVERY_RATE_MAX)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "crDegradationShortWithFinal")
                .formatWithOneDecimalAndModifier("percent", CR_LOSS_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", CR_LOSS_MAX)
                .addToTooltip(tooltip);

    }

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {

        float delta;
        if (fm.getHullSpec().getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
            delta = CAPITAL_MULT;
        } else if (fm.getHullSpec().getHullSize() == ShipAPI.HullSize.CRUISER) {
            delta = CRUISER_MULT;
        } else if (fm.getHullSpec().getHullSize() == ShipAPI.HullSize.DESTROYER) {
            delta = DESTROYER_MULT;
        } else {
            delta = FRIGATE_MULT;
        }

        StatUtils.setStatPercent(stats.getPeakCRDuration(), this.getBuffId(), level, PEAK_CR_MAX * delta, maxLevel);
        StatUtils.setStatMult(stats.getCRPerDeploymentPercent(), this.getBuffId(), level, CR_TO_DEPLOY_MAX, maxLevel);

        StatUtils.setStatPercent(stats.getMinCrewMod(), this.getBuffId(), level, REQUIRED_CREW_MAX, maxLevel);

        if (level >= 3) {
            StatUtils.setStatPercent(stats.getCRLossPerSecondPercent(), this.getBuffId(), level - 2, CR_LOSS_MAX, maxLevel - 2);
            StatUtils.setStatMult(stats.getBaseCRRecoveryRatePercentPerDay(), this.getBuffId(), level - 2, CR_RECOVERY_RATE_MAX, maxLevel - 2);
        }
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (expand) {
            tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

            this.addIncreaseWithFinalToTooltip(tooltip,
                    "peakPerformanceTime",
                    fm.getStats().getPeakCRDuration().getPercentBonus(this.getBuffId()).getValue(),
                    fm.getVariant().getHullSpec().getNoCRLossTime());

            float crPerDeploymentMult = fm.getStats().getCRPerDeploymentPercent().getMultBonus(this.getBuffId()).getValue();
            this.addDecreaseWithFinalToTooltip(tooltip,
                    "crPerDeployment",
                    crPerDeploymentMult,
                    fm.getHullSpec().getCRToDeploy());

            this.addIncreaseWithFinalToTooltip(tooltip,
                    "requiredCrew",
                    fm.getStats().getMinCrewMod().getPercentBonus(this.getBuffId()).getValue(),
                    fm.getHullSpec().getMinCrew());

            MutableStat.StatMod baseCRRecoveryStat = fm.getStats().getBaseCRRecoveryRatePercentPerDay().getMultStatMod(this.getBuffId());
            float baseCRRecoveryBonus = 1f;
            if (baseCRRecoveryStat != null) {
                baseCRRecoveryBonus = baseCRRecoveryStat.getValue();
            }

            this.addDecreaseWithFinalToTooltip(tooltip,
                    "crRecoveryRate",
                    baseCRRecoveryBonus,
                    fm.getStats().getBaseCRRecoveryRatePercentPerDay().getBaseValue());

            MutableStat.StatMod crDegradationStat = fm.getStats().getCRLossPerSecondPercent().getPercentBonus(this.getBuffId());
            float crDegradationBonus = 0f;
            if (crDegradationStat != null) {
                crDegradationBonus = crDegradationStat.getValue();
            }

            this.addIncreaseToTooltip(tooltip,
                    "crDegradation",
                    crDegradationBonus);
        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }
    @Override
    public float getSpawnChance() {
        return super.getSpawnChance() * 0.5f;
    }
}
