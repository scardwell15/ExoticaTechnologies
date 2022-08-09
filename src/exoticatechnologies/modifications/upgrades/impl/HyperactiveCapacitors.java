package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StatUtils;
import exoticatechnologies.util.StringUtils;
import lombok.Getter;

import java.awt.*;

public class HyperactiveCapacitors extends Upgrade {
    @Getter protected final float bandwidthUsage = 15f;
    private static final float CAPACITY_MAX = 25f;
    private static final float VENT_SPEED_MAX = 50f;
    private static final float WEAPON_FLUX_MAX = 24f;
    private static final Color COLOR = new Color(143, 86, 182);

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatMult(stats.getFluxCapacity(), this.getBuffId(), level, CAPACITY_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getVentRateMult(), this.getBuffId(), level, VENT_SPEED_MAX, maxLevel);

        if (level >= 3) {
            StatUtils.setStatPercent(stats.getBallisticWeaponFluxCostMod(), this.getBuffId(), level - 2, WEAPON_FLUX_MAX, maxLevel - 2);
            StatUtils.setStatPercent(stats.getEnergyWeaponFluxCostMod(), this.getBuffId(), level - 2, WEAPON_FLUX_MAX, maxLevel - 2);
            StatUtils.setStatPercent(stats.getMissileWeaponFluxCostMod(), this.getBuffId(), level - 2, WEAPON_FLUX_MAX, maxLevel - 2);
        }
    }

    @Override
    public void printStatInfoToTooltip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications mods) {
        this.addBenefitToShopTooltipMult(tooltip, "fluxCapacity", fm, mods, CAPACITY_MAX);
        this.addBenefitToShopTooltip(tooltip, "ventSpeed", fm, mods, VENT_SPEED_MAX);

        //after level 3
        StringUtils.getTranslation("ShipListDialog", "UpgradeDrawbackAfterLevel")
                .format("level", 3)
                .addToTooltip(tooltip);

        this.addMalusToShopTooltip(tooltip, "weaponFluxCosts", fm, mods, 3, WEAPON_FLUX_MAX);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (level > 0) {
            if(expand) {
                tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

                this.addBenefitToTooltipMult(tooltip,
                        "fluxCapacity",
                        fm.getStats().getFluxCapacity().getMultStatMod(this.getBuffId()).getValue());

                this.addBenefitToTooltip(tooltip,
                        "ventSpeed",
                        fm.getStats().getVentRateMult().getPercentStatMod(this.getBuffId()).getValue());


                MutableStat.StatMod weaponFluxStat = fm.getStats().getBallisticWeaponFluxCostMod().getPercentBonus(this.getBuffId());
                float weaponFluxBonus = 0f;
                if (weaponFluxStat != null) {
                    weaponFluxBonus = weaponFluxStat.getValue();
                }

                this.addMalusToTooltip(tooltip,
                        "weaponFluxCosts",
                        weaponFluxBonus);
            } else {
                tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
            }
        }
    }
}
