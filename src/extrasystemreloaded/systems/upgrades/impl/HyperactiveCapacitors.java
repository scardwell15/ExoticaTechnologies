package extrasystemreloaded.systems.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StatUtils;
import lombok.Getter;

import java.awt.*;

public class HyperactiveCapacitors extends Upgrade {
    @Getter protected final float bandwidthUsage = 15f;
    private static float CAPACITY_MAX = 25f;
    private static float VENT_SPEED_MAX = 50f;
    private static float WEAPON_FLUX_MAX = 30f;

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatMult(stats.getFluxCapacity(), this.getBuffId(), level, CAPACITY_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getVentRateMult(), this.getBuffId(), level, VENT_SPEED_MAX, maxLevel);

        StatUtils.setStatPercent(stats.getBallisticWeaponFluxCostMod(), this.getBuffId(), level, WEAPON_FLUX_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getEnergyWeaponFluxCostMod(), this.getBuffId(), level, WEAPON_FLUX_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getMissileWeaponFluxCostMod(), this.getBuffId(), level, WEAPON_FLUX_MAX, maxLevel);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ExtraSystems systems, boolean expand) {
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

                this.addIncreaseToTooltip(tooltip,
                        "weaponFluxCosts",
                        fm.getStats().getBallisticWeaponFluxCostMod().getPercentBonus(this.getBuffId()).getValue());
            } else {
                tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
            }
        }
    }
}
