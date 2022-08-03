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

public class WeldedArmor extends Upgrade {
    @Getter protected final float bandwidthUsage = 15f;
    private static final float HULL_MAX = 30f;
    private static final float ARMOR_MAX = 10f;

    private static final float EMP_TAKEN_MAX = 16f;
    private static final float ENGINE_HEALTH_MAX = -16f;
    private static final float WEAPON_HEALTH_MAX = -16f;
    private static final Color COLOR = new Color(88, 225, 61);

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public void printStatInfoToTooltip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications mods) {
        this.addBenefitToShopTooltip(tooltip, "hull", fm, mods, HULL_MAX);
        this.addBenefitToShopTooltip(tooltip, "armor", fm, mods, ARMOR_MAX);

        StringUtils.getTranslation("ShipListDialog", "UpgradeDrawbackAfterLevel")
                .format("level", 3)
                .addToTooltip(tooltip);

        this.addMalusToShopTooltipMult(tooltip, "weaponHealth", fm, mods, 3, WEAPON_HEALTH_MAX);
        this.addMalusToShopTooltipMult(tooltip, "engineHealth", fm, mods, 3, ENGINE_HEALTH_MAX);
        this.addMalusToShopTooltip(tooltip, "empDamage", fm, mods, 3, EMP_TAKEN_MAX);
    }

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatPercent(stats.getHullBonus(), this.getBuffId(), level, HULL_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getArmorBonus(), this.getBuffId(), level, ARMOR_MAX, maxLevel);

        if (level >= 3) {
            StatUtils.setStatPercent(stats.getEmpDamageTakenMult(), this.getBuffId(), level - 2, EMP_TAKEN_MAX, maxLevel - 2);
            StatUtils.setStatMult(stats.getEngineHealthBonus(), this.getBuffId(), level - 2, ENGINE_HEALTH_MAX, maxLevel - 2);
            StatUtils.setStatMult(stats.getWeaponHealthBonus(), this.getBuffId(), level - 2, WEAPON_HEALTH_MAX, maxLevel - 2);
        }
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (expand) {
            tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

            this.addBenefitToTooltip(tooltip,
                    "hull",
                    fm.getStats().getHullBonus().getPercentBonus(this.getBuffId()).getValue(),
                    fm.getVariant().getHullSpec().getHitpoints());

            this.addBenefitToTooltip(tooltip,
                    "armor",
                    fm.getStats().getArmorBonus().getPercentBonus(this.getBuffId()).getValue(),
                    fm.getVariant().getHullSpec().getArmorRating());

            MutableStat.StatMod empDamageTakenStat = fm.getStats().getEmpDamageTakenMult().getPercentStatMod(this.getBuffId());
            float empDamageBonus = 0f;
            if (empDamageTakenStat != null) {
                empDamageBonus = empDamageTakenStat.getValue();
            }

            this.addMalusToTooltip(tooltip,
                    "empDamage",
                    empDamageBonus);

            MutableStat.StatMod engineHealthBonusStat = fm.getStats().getWeaponHealthBonus().getMultBonus(this.getBuffId());
            float engineHealthBonus = 1f;
            if (engineHealthBonusStat != null) {
                engineHealthBonus = engineHealthBonusStat.getValue();
            }

            this.addMalusToTooltipMult(tooltip,
                    "engineHealth",
                    engineHealthBonus);

            MutableStat.StatMod weaponHealthBonusStat = fm.getStats().getWeaponHealthBonus().getMultBonus(this.getBuffId());
            float weaponHealthBonus = 1f;
            if (weaponHealthBonusStat != null) {
                weaponHealthBonus = weaponHealthBonusStat.getValue();
            }

            this.addMalusToTooltipMult(tooltip,
                    "weaponHealth",
                    weaponHealthBonus);
        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }
}
