package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;

import java.awt.*;

public class TracerRecoilCalculator extends Upgrade {
    @Getter protected final float bandwidthUsage = 5f;
    private static final float RECOIL_REDUCTION = -30f;
    private static final float WEAPON_MOUNT_HEALTH = 16f;
    private static final float WEAPON_TURN_RATE = -16f;
    private static final Color COLOR = new Color(213, 129, 60);

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatMult(stats.getRecoilPerShotMultSmallWeaponsOnly(), this.getBuffId(), level, RECOIL_REDUCTION, maxLevel);
        StatUtils.setStatMult(stats.getRecoilPerShotMult(), this.getBuffId(), level, RECOIL_REDUCTION, maxLevel);
        StatUtils.setStatMult(stats.getMaxRecoilMult(), this.getBuffId(), level, RECOIL_REDUCTION, maxLevel);
        StatUtils.setStatPercent(stats.getWeaponHealthBonus(), this.getBuffId(), level, WEAPON_MOUNT_HEALTH, maxLevel);

        if (level >= 3) {
            StatUtils.setStatMult(stats.getWeaponTurnRateBonus(), this.getBuffId(), level - 2, WEAPON_TURN_RATE, maxLevel - 2);
        }
    }

    @Override
    public void printStatInfoToTooltip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications mods) {
        this.addBenefitToShopTooltipMult(tooltip, "recoil", fm, mods, RECOIL_REDUCTION);
        this.addBenefitToShopTooltipMult(tooltip, "weaponHealth", fm, mods, WEAPON_MOUNT_HEALTH);

        //after level 3
        StringUtils.getTranslation("UpgradesDialog", "UpgradeDrawbackAfterLevel")
                .format("level", 3)
                .addToTooltip(tooltip);

        this.addMalusToShopTooltipMult(tooltip, "weaponTurnRate", fm, mods, 3, WEAPON_TURN_RATE);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (expand) {
            tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

            this.addBenefitToTooltipMult(tooltip,
                    "recoil",
                    fm.getStats().getRecoilPerShotMult().getMultStatMod(this.getBuffId()).getValue());

            this.addBenefitToTooltip(tooltip,
                    "weaponHealth",
                    fm.getStats().getWeaponHealthBonus().getPercentBonus(this.getBuffId()).getValue());

            MutableStat.StatMod weaponTurnStat = fm.getStats().getWeaponTurnRateBonus().getMultBonus(this.getBuffId());
            float weaponTurnRate = 1f;
            if (weaponTurnStat != null) {
                weaponTurnRate = weaponTurnStat.getValue();
            }

            this.addMalusToTooltipMult(tooltip,
                    "weaponTurnRate",
                    weaponTurnRate);
        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }
}
