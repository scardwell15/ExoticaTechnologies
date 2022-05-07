package extrasystemreloaded.systems.upgrades.impl;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StatUtils;
import extrasystemreloaded.systems.upgrades.Upgrade;
import lombok.Getter;

import java.awt.*;

public class ForcedOvertime extends Upgrade {
    @Getter protected final float bandwidthUsage = 5f;
    private static float CR_TO_DEPLOY_MAX = -20f;
    private static float CR_RECOVERY_RATE_MAX = -20f;

    private static float REQUIRED_CREW_MAX = 20f;
    private static float PEAK_CR_MAX = 20f;
    private static float CR_LOSS_MAX = -20f;

    private static float FRIGATE_MULT = 8f;
    private static float DESTROYER_MULT = 3f;
    private static float CRUISER_MULT = 1.5f;
    private static float CAPITAL_MULT = 1f;

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
        StatUtils.setStatMult(stats.getCRLossPerSecondPercent(), this.getBuffId(), level, CR_LOSS_MAX, maxLevel);
        StatUtils.setStatMult(stats.getBaseCRRecoveryRatePercentPerDay(), this.getBuffId(), level, CR_RECOVERY_RATE_MAX, maxLevel);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ExtraSystems systems, boolean expand) {
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

            this.addDecreaseWithFinalToTooltip(tooltip,
                    "crRecoveryRate",
                    fm.getStats().getBaseCRRecoveryRatePercentPerDay().getMultStatMod(this.getBuffId()).getValue(),
                    fm.getStats().getBaseCRRecoveryRatePercentPerDay().getBaseValue());

            this.addIncreaseToTooltip(tooltip,
                    "crDegradation",
                    fm.getStats().getCRLossPerSecondPercent().getMultBonus(this.getBuffId()).getValue());

            this.addIncreaseWithFinalToTooltip(tooltip,
                    "requiredCrew",
                    fm.getStats().getMinCrewMod().getPercentBonus(this.getBuffId()).getValue(),
                    fm.getHullSpec().getMinCrew());
        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }
}
