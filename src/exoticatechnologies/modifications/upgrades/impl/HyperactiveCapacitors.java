package exoticatechnologies.modifications.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.util.StatUtils;
import lombok.Getter;

import java.awt.*;

public class HyperactiveCapacitors extends Upgrade {
    @Getter protected final float bandwidthUsage = 15f;
    private static float CAPACITY_MAX = 25f;
    private static float VENT_SPEED_MAX = 50f;
    private static float WEAPON_FLUX_MAX = 30f;
    private static Color COLOR = new Color(143, 86, 182);

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
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ShipModifications systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (level > 0) {
            if(expand) {
                tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

                this.addIncreaseToTooltip(tooltip,
                        "fluxCapacity",
                        (fm.getStats().getFluxCapacity().getMultStatMod(this.getBuffId()).getValue() - 1f) * 100f);

                this.addIncreaseToTooltip(tooltip,
                        "ventSpeed",
                        fm.getStats().getVentRateMult().getPercentStatMod(this.getBuffId()).getValue());


                MutableStat.StatMod weaponFluxStat = fm.getStats().getBallisticWeaponFluxCostMod().getPercentBonus(this.getBuffId());
                float weaponFluxBonus = 0f;
                if (weaponFluxStat != null) {
                    weaponFluxBonus = weaponFluxStat.getValue();
                }

                this.addIncreaseToTooltip(tooltip,
                        "weaponFluxCosts",
                        weaponFluxBonus);
            } else {
                tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
            }
        }
    }
}
