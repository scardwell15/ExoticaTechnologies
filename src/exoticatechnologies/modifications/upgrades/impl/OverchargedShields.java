package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;

import java.awt.*;

public class OverchargedShields extends Upgrade {
    @Getter protected final float bandwidthUsage = 15f;
    private static final float FLUX_PER_DAM_MAX = -20f;
    private static final float ARC_MAX = 40f;
    private static float UPKEEP_MAX = 36f;
    private static final float UNFOLD_MAX = -30f;
    private static final float TURNRATE_MAX = -25f;
    private static final Color COLOR = new Color(56, 187, 166);

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public boolean canApply(ShipVariantAPI var) {
        if (!ShieldAPI.ShieldType.FRONT.equals(var.getHullSpec().getDefenseType())
                && !ShieldAPI.ShieldType.OMNI.equals(var.getHullSpec().getDefenseType())) {
            return false;
        }
        return super.canApply(var);
    }

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatMult(stats.getShieldDamageTakenMult(), this.getBuffId(), level, FLUX_PER_DAM_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getShieldArcBonus(), this.getBuffId(), level, ARC_MAX, maxLevel);

        if (level >= 3) {
            if (fm.getHullSpec().getShieldSpec().getUpkeepCost() >= 300) {
                UPKEEP_MAX = UPKEEP_MAX * 0.66f;
            }

            StatUtils.setStatPercent(stats.getShieldUpkeepMult(), this.getBuffId(), level - 2, UPKEEP_MAX, maxLevel - 2);
            StatUtils.setStatMult(stats.getShieldUnfoldRateMult(), this.getBuffId(), level - 2, UNFOLD_MAX, maxLevel - 2);
            StatUtils.setStatMult(stats.getShieldTurnRateMult(), this.getBuffId(), level - 2, TURNRATE_MAX, maxLevel - 2);
        }
    }

    @Override
    public void printStatInfoToTooltip(FleetMemberAPI fm, TooltipMakerAPI tooltip) {
        StringUtils.getTranslation(this.getKey(), "shieldEfficiency")
                .formatWithOneDecimalAndModifier("percent", FLUX_PER_DAM_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", FLUX_PER_DAM_MAX)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "shieldArc")
                .formatWithOneDecimalAndModifier("percent", ARC_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", ARC_MAX)
                .addToTooltip(tooltip);

        //after level 3
        StringUtils.getTranslation("ShipListDialog", "UpgradeDrawbackAfterLevel")
                .format("level", 3)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "shieldUpkeep")
                .formatWithOneDecimalAndModifier("percent", UPKEEP_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", UPKEEP_MAX)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "shieldUnfoldRateWithFinal")
                .formatWithOneDecimalAndModifier("percent", UNFOLD_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", UNFOLD_MAX)
                .addToTooltip(tooltip);

        StringUtils.getTranslation(this.getKey(), "shieldTurnRateWithFinal")
                .formatWithOneDecimalAndModifier("percent", TURNRATE_MAX / this.getMaxLevel(fm))
                .formatPercWithOneDecimalAndModifier("finalValue", TURNRATE_MAX)
                .addToTooltip(tooltip);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (level > 0) {
            if(expand) {
                tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

                float shieldEffMod = fm.getStats().getShieldDamageTakenMult().getMultStatMod(this.getBuffId()).getValue();
                float shieldEffMult = -(1f - shieldEffMod);
                float shieldEffFinal = fm.getHullSpec().getBaseShieldFluxPerDamageAbsorbed() * shieldEffMult;
                StringUtils.getTranslation(this.getKey(), "shieldEfficiency")
                        .formatWithOneDecimalAndModifier("percent", shieldEffMult * 100f)
                        .format("finalValue", String.valueOf(Math.round(shieldEffFinal * 100f) / 100f))
                        .addToTooltip(tooltip, 2f);

                this.addIncreaseWithFinalToTooltip(tooltip,
                        "shieldArc",
                        fm.getStats().getShieldArcBonus().getPercentBonus(this.getBuffId()).getValue(),
                        fm.getHullSpec().getShieldSpec().getArc());

                MutableStat.StatMod shieldUpkeepStat = fm.getStats().getShieldUpkeepMult().getPercentStatMod(this.getBuffId());
                float shieldUpkeepBonus = 0f;
                if (shieldUpkeepStat != null) {
                    shieldUpkeepBonus = shieldUpkeepStat.getValue();
                }

                this.addIncreaseWithFinalToTooltip(tooltip,
                        "shieldUpkeep",
                        shieldUpkeepBonus,
                        fm.getHullSpec().getShieldSpec().getUpkeepCost());

                MutableStat.StatMod shieldUnfoldStat = fm.getStats().getShieldUnfoldRateMult().getMultStatMod(this.getBuffId());
                float shieldUnfoldBonus = 1f;
                if (shieldUnfoldStat != null) {
                    shieldUnfoldBonus = shieldUnfoldStat.getValue();
                }

                this.addDecreaseToTooltip(tooltip,
                        "shieldUnfoldRate",
                        shieldUnfoldBonus);


                MutableStat.StatMod shieldTurnStat = fm.getStats().getShieldTurnRateMult().getMultStatMod(this.getBuffId());
                float shieldTurnBonus = 1f;
                if (shieldTurnStat != null) {
                    shieldTurnBonus = shieldTurnStat.getValue();
                }

                this.addDecreaseToTooltip(tooltip,
                        "shieldTurnRate",
                        shieldTurnBonus);
            } else {
                tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
            }
        }
    }
}
