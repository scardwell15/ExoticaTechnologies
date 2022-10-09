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
    private static final float BURN_LEVEL_MAX = 30f;

    private static final float DECELERATION_MAX = -24f;
    private static final float FUEL_USE_MAX = 16f;
    private static final Color COLOR = new Color(255, 224, 94);

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public void printStatInfoToTooltip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications mods) {
        this.addBenefitToShopTooltip(tooltip, "speed", fm, mods, SPEED_MULT);
        this.addBenefitToShopTooltip(tooltip, "acceleration", fm, mods, ACCELERATION_MAX);
        this.addBenefitToShopTooltip(tooltip, "burnLevel", fm, mods, BURN_LEVEL_MAX);
        this.addBenefitToShopTooltip(tooltip, "turnrate", fm, mods, TURN_RATE_MAX);

        StringUtils.getTranslation("ShipListDialog", "UpgradeDrawbackAfterLevel")
                .format("level", 3)
                .addToTooltip(tooltip);

        this.addMalusToShopTooltipMult(tooltip, "deceleration", fm, mods, 3, DECELERATION_MAX);
        this.addMalusToShopTooltip(tooltip, "fuelUse", fm, mods, 3, FUEL_USE_MAX);
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

                this.addBenefitToTooltip(tooltip,
                        "speed",
                        fm.getStats().getMaxSpeed().getPercentStatMod(this.getBuffId()).getValue(),
                        fm.getStats().getMaxSpeed().getBaseValue());

                this.addBenefitToTooltip(tooltip,
                        "acceleration",
                        fm.getStats().getAcceleration().getPercentStatMod(this.getBuffId()).getValue());

                this.addBenefitToTooltip(tooltip,
                        "turnrate",
                        fm.getStats().getMaxTurnRate().getPercentStatMod(this.getBuffId()).getValue());

                this.addBenefitToTooltip(tooltip,
                        "burnLevel",
                        fm.getStats().getMaxBurnLevel().getPercentStatMod(this.getBuffId()).getValue(),
                        fm.getStats().getMaxBurnLevel().getBaseValue());



                MutableStat.StatMod decelerationStat = fm.getStats().getDeceleration().getMultStatMod(this.getBuffId());
                float decelerationBonus = 1f;
                if (decelerationStat != null) {
                    decelerationBonus = decelerationStat.getValue();
                }

                this.addMalusToTooltipMult(tooltip,
                        "deceleration",
                        decelerationBonus);

                MutableStat.StatMod fuelUseStat = fm.getStats().getFuelUseMod().getPercentBonus(this.getBuffId());
                float fuelUseBonus = 0f;
                if (fuelUseStat != null) {
                    fuelUseBonus = fuelUseStat.getValue();
                }

                this.addMalusToTooltip(tooltip,
                        "fuelUse",
                        fuelUseBonus,
                        fm.getHullSpec().getFuelPerLY());
            } else {
                tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
            }
        }
    }
}
