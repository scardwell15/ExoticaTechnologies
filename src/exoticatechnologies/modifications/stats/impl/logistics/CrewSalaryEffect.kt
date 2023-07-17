package exoticatechnologies.modifications.stats.impl.logistics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.integration.ironshell.IronShellIntegration
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeModEffect
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import kotlin.math.round
import kotlin.math.roundToInt

class CrewSalaryEffect : UpgradeModEffect() {
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "crewSalary"

    override fun getEffectiveValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        return 10 + round(getCurrentEffect(member, mods, mod))
    }

    override fun printToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        val salary: Int = 10 + getCurrentEffect(member, mods, mod).roundToInt()
        val totalCostPerMonth: Int = getIncreasedSalaryForMember(member, mods, mod)
        return StringUtils.getTranslation("ModEffects", "crewSalary")
            .format("salaryIncrease", salary)
            .format("finalValue", totalCostPerMonth)
            .addToTooltip(tooltip, 2f)
    }

    override fun printToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        val salary: Int = 10 + getCurrentEffect(member, mods, mod).roundToInt()
        val totalCostPerMonth: Int = getIncreasedSalaryForMember(member, mods, mod)

        val translation: StringUtils.Translation
        if (IronShellIntegration.isEnabled()) {
            translation = StringUtils.getTranslation("ModEffects", "crewSalaryShopIronShell")
        } else {
            translation = StringUtils.getTranslation("ModEffects", "crewSalaryShop")
        }

        return translation
            .format("salaryIncrease", salary)
            .formatWithModifier("perLevel", getPerLevelEffect(member, mods, mod))
            .format("finalValue", totalCostPerMonth)
            .addToTooltip(tooltip)
    }

    companion object {
        fun getIncreasedSalaryForMember(member: FleetMemberAPI, mods: ShipModifications): Int {
            return mods.getUpgradeMap().keys.sumOf { getIncreasedSalaryForMember(member, mods, it) }
        }

        fun getIncreasedSalaryForMember(member: FleetMemberAPI, mods: ShipModifications, mod: Upgrade): Int {
            getCrewSalaryEffect(mod.upgradeEffects)?.let {
                val actualCrew = member.minCrew
                return (it.getCurrentEffect(member, mods, mod) * actualCrew).roundToInt()
            }
            return 0
        }

        fun getCrewSalaryEffect(list: List<UpgradeModEffect>): UpgradeModEffect? {
            return list.firstOrNull { it.key == "crewSalary" }
        }

        fun addListenerIfNotAdded() {
            if (!Global.getSector().listenerManager.hasListener(SalaryListener::class)) {
                Global.getSector().listenerManager.addListener(SalaryListener())
            }
        }
    }

    open class SalaryListener : EconomyTickListener, TooltipMakerAPI.TooltipCreator {
        override fun reportEconomyTick(iterIndex: Int) {
            val lastIterInMonth = Global.getSettings().getFloat("economyIterPerMonth").toInt() - 1
            if (iterIndex != lastIterInMonth) return

            val salaryCommission = salaryCommission.toFloat()
            if (salaryCommission <= 0f) {
                return
            }

            if (IronShellIntegration.isEnabled()) {
                IronShellIntegration.setSalaryTax(salaryCommission)
            }
            val report = SharedData.getData().currentReport
            val fleetNode = report.getNode(MonthlyReport.FLEET)
            val commissionedCrewsNode = report.getNode(fleetNode, "ET_CC_stipend")
            commissionedCrewsNode.upkeep = salaryCommission
            commissionedCrewsNode.name = "Salaries for Commissioned Crews"
            commissionedCrewsNode.icon = Global.getSettings().getSpriteName("income_report", "crew")
            commissionedCrewsNode.tooltipCreator = this
        }

        override fun reportEconomyMonthEnd() {}

        private val salaryCommission: Int
            get() {
                val fleet = Global.getSector().playerFleet ?: return 0
                var increasedSalary = 0
                for (member in fleet.membersWithFightersCopy) {
                    val mods = ShipModLoader.get(member, member.variant)
                    if (mods != null) {
                        increasedSalary += getIncreasedSalaryForMember(member, mods)
                    }
                }
                return increasedSalary
            }

        override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
            tooltip.addPara(
                "Monthly cost of commissioned crews: %s credits",
                0f, Misc.getHighlightColor(), Misc.getDGSCredits(salaryCommission.toFloat())
            )
        }

        override fun getTooltipWidth(tooltipParam: Any): Float {
            return 450f
        }

        override fun isTooltipExpandable(tooltipParam: Any): Boolean {
            return false
        }
    }
}