package extrasystemreloaded.systems.upgrades.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import extrasystemreloaded.Es_ModPlugin;
import extrasystemreloaded.util.ExtraSystems;
import extrasystemreloaded.util.StatUtils;
import extrasystemreloaded.systems.upgrades.Upgrade;
import extrasystemreloaded.util.StringUtils;
import lombok.Getter;

import java.awt.*;

public class CommissionedCrews extends Upgrade {
    @Getter protected final float bandwidthUsage = 5f;
    private static int COST_PER_CREW_MAX = 10; //in addition to base crew salary (10 credits)
    private static float SUPPLIES_MONTH_MAX = -30f;
    private static float SUPPLIES_RECOVERY_MAX = 20f;
    private static float REPAIR_RATE_MAX = 20f;
    private static float FUEL_USE_MAX = -20f;

    @Override
    public boolean canApply(ShipVariantAPI var) {
        if (var.getHullSpec().getMinCrew() == 0) {
            return false;
        }
        return super.canApply(var);
    }

    public int getIncreasedSalaryForMember(FleetMemberAPI fm, ExtraSystems es) {
        float actualCrew = fm.getMinCrew();
        float effectiveCrew;
        if(actualCrew > 3333) {
            effectiveCrew = 1666; //at 3333 actual crew the formula below starts going down
        } else {
            effectiveCrew = actualCrew - 0.00015f * actualCrew * actualCrew;
        }

        float level = es.getUpgrade(this);
        float maxLevel = this.getMaxLevel(fm);
        int salary = (int) Math.ceil(level / maxLevel * COST_PER_CREW_MAX * effectiveCrew);

        return salary;
    }

    private static boolean doesEconomyHaveListener() {
        return Global.getSector().getListenerManager().hasListenerOfClass(CommissionedSalaryListener.class);
    }

    @Override
    public void applyUpgradeToStats(FleetMemberAPI fm, MutableShipStatsAPI stats, int level, int maxLevel) {
        StatUtils.setStatMult(stats.getSuppliesPerMonth(), this.getBuffId(), level, SUPPLIES_MONTH_MAX, maxLevel);
        StatUtils.setStatMult(stats.getSuppliesToRecover(), this.getBuffId(), level, SUPPLIES_RECOVERY_MAX, maxLevel);
        StatUtils.setStatPercent(stats.getRepairRatePercentPerDay(), this.getBuffId(), level, REPAIR_RATE_MAX, maxLevel);
        StatUtils.setStatMult(stats.getFuelUseMod(), this.getBuffId(), level, FUEL_USE_MAX, maxLevel);
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI fm, int level, int maxLevel) {
        if(!doesEconomyHaveListener()) {
            Global.getSector().getListenerManager().addListener(new CommissionedSalaryListener());
        }
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, FleetMemberAPI fm, ExtraSystems systems, boolean expand) {
        int level = systems.getUpgrade(this);

        if (expand) {
            tooltip.addPara(this.getName() + " (%s):", 5, this.getColor(), String.valueOf(level));

            int salary = 10 + COST_PER_CREW_MAX * level / getMaxLevel(fm);
            int totalCostPerMonth = getIncreasedSalaryForMember(fm, systems);
            StringUtils.getTranslation(this.getKey(), "crewSalary")
                    .format("salaryIncrease", salary)
                    .format("finalValue", totalCostPerMonth)
                    .addToTooltip(tooltip, 2f);


            float fuelUseMult = fm.getStats().getSuppliesToRecover().getMultStatMod(this.getBuffId()).getValue();
            float savedFuel = fm.getHullSpec().getFuelPerLY() * fuelUseMult;
            float fuelCost = Global.getSector().getEconomy().getCommoditySpec(Commodities.FUEL).getBasePrice();
            StringUtils.getTranslation(this.getKey(), "fuelConsumption")
                    .format("percent", (1f - fuelUseMult) * -100f)
                    .format("finalValue", fm.getHullSpec().getFuelPerLY() * fuelUseMult)
                    .format("creditsSavedPerMonth", savedFuel * fuelCost)
                    .addToTooltip(tooltip, 2f);

            float supplyUseMult = fm.getStats().getSuppliesPerMonth().getMultStatMod(this.getBuffId()).getValue();
            float savedSupply = fm.getHullSpec().getSuppliesPerMonth() * supplyUseMult;
            float supplyCost = Global.getSector().getEconomy().getCommoditySpec(Commodities.SUPPLIES).getBasePrice();
            StringUtils.getTranslation(this.getKey(), "supplyConsumption")
                    .format("percent", (1f - supplyUseMult) * -100f)
                    .format("finalValue", fm.getHullSpec().getSuppliesPerMonth() * supplyUseMult)
                    .format("creditsSavedPerMonth", savedSupply * supplyCost)
                    .addToTooltip(tooltip, 2f);

            this.addDecreaseWithFinalToTooltip(tooltip,
                    "suppliesToRecover",
                    fm.getStats().getSuppliesToRecover().getMultStatMod(this.getBuffId()).getValue(),
                    fm.getHullSpec().getSuppliesToRecover());

            this.addIncreaseWithFinalToTooltip(tooltip,
                    "hullRepair",
                    fm.getStats().getRepairRatePercentPerDay().getPercentStatMod(this.getBuffId()).getValue(),
                    fm.getStats().getRepairRatePercentPerDay().getBaseValue());

        } else {
            tooltip.addPara(this.getName() + " (%s)", 5, this.getColor(), String.valueOf(level));
        }
    }

    public class CommissionedSalaryListener implements EconomyTickListener, TooltipMakerAPI.TooltipCreator {
        public void reportEconomyTick(int iterIndex) {
            int lastIterInMonth = (int) Global.getSettings().getFloat("economyIterPerMonth") - 1;
            if (iterIndex != lastIterInMonth) return;

            //all upgrades removed
            int salaryCommission = getSalaryCommission();
            if (salaryCommission <= 0f) {
                Global.getSector().getListenerManager().removeListener(this);
                return;
            }

            MonthlyReport report = SharedData.getData().getCurrentReport();

            MonthlyReport.FDNode fleetNode = report.getNode(MonthlyReport.FLEET);

            MonthlyReport.FDNode commissionedCrewsNode = report.getNode(fleetNode, "ESR_CC_stipend");
            commissionedCrewsNode.upkeep = salaryCommission;
            commissionedCrewsNode.name = "Salaries for Commissioned Crews";
            commissionedCrewsNode.icon = Global.getSettings().getSpriteName("income_report", "crew");
            commissionedCrewsNode.tooltipCreator = this;
        }

        public void reportEconomyMonthEnd() {
        }

        private int getSalaryCommission() {
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            if(fleet == null) {
                return 0;
            }

            int increasedSalary = 0;
            for(FleetMemberAPI fm : fleet.getMembersWithFightersCopy()) {
                if(Es_ModPlugin.hasData(fm.getId())) {

                    ExtraSystems es = ExtraSystems.getForFleetMember(fm);
                    if(es.getUpgrade(CommissionedCrews.this.getKey()) > 0) {
                        increasedSalary += CommissionedCrews.getInstance().getIncreasedSalaryForMember(fm, es);
                    }
                }
            }

            return increasedSalary;
        }


        public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
            tooltip.addPara("Monthly cost of commissioned crews: %s credits",
                    0f, Misc.getHighlightColor(), Misc.getDGSCredits(getSalaryCommission()));
        }

        public float getTooltipWidth(Object tooltipParam) {
            return 450;
        }

        public boolean isTooltipExpandable(Object tooltipParam) {
            return false;
        }
    }

    public static CommissionedCrews getInstance() {
        return getInstance(CommissionedCrews.class);
    }
}
