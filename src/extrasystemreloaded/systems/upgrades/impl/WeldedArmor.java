package extrasystemreloaded.systems.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StatUtils;
import extrasystemreloaded.systems.upgrades.Upgrade;
import lombok.Getter;

import java.awt.*;

public class WeldedArmor extends Upgrade {
    @Getter protected final float bandwidthUsage = 15f;
    private static float HULL_MAX = 30f;
    private static float ARMOR_MAX = 10f;

    private static float EMP_TAKEN_MAX = 25f;
    private static float ENGINE_HEALTH_MAX = -15f;
    private static float WEAPON_HEALTH_MAX = -15f;

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatPercent(stats.getHullBonus(), this.getBuffId(), level, HULL_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getArmorBonus(), this.getBuffId(), level, ARMOR_MAX, maxLevel);

        StatUtils.setStatPercent(stats.getEmpDamageTakenMult(), this.getBuffId(), level, EMP_TAKEN_MAX, maxLevel);
        StatUtils.setStatMult(stats.getEngineHealthBonus(), this.getBuffId(), level, ENGINE_HEALTH_MAX, maxLevel);
        StatUtils.setStatMult(stats.getWeaponHealthBonus(), this.getBuffId(), level, WEAPON_HEALTH_MAX, maxLevel);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ExtraSystems systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (expand) {
            tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

            this.addIncreaseWithFinalToTooltip(tooltip,
                    "hull",
                    fm.getStats().getHullBonus().getPercentBonus(this.getBuffId()).getValue(),
                    fm.getVariant().getHullSpec().getHitpoints());

            this.addIncreaseWithFinalToTooltip(tooltip,
                    "armor",
                    fm.getStats().getArmorBonus().getPercentBonus(this.getBuffId()).getValue(),
                    fm.getVariant().getHullSpec().getArmorRating());

            this.addIncreaseToTooltip(tooltip,
                    "empDamage",
                    fm.getStats().getEmpDamageTakenMult().getPercentStatMod(this.getBuffId()).getValue());

            this.addDecreaseToTooltip(tooltip,
                    "engineHealth",
                    fm.getStats().getEngineHealthBonus().getMultBonus(this.getBuffId()).getValue());

            this.addDecreaseToTooltip(tooltip,
                    "weaponHealth",
                    fm.getStats().getWeaponHealthBonus().getMultBonus(this.getBuffId()).getValue());
        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }
}
