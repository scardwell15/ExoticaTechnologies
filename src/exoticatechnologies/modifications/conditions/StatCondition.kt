package exoticatechnologies.modifications.conditions

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeModEffect
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect
import exoticatechnologies.modifications.stats.UpgradeStatBonusWithFinalEffect

class StatCondition(var stat: UpgradeModEffect) : OperatorCondition() {
    override val key = "!!!stat"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?, variant: ShipVariantAPI): Any? {
        if (stat is UpgradeMutableStatEffect) {
            return (stat as UpgradeMutableStatEffect).getStat(member.stats).baseValue
        } else if (stat is UpgradeStatBonusWithFinalEffect) {
            return (stat as UpgradeStatBonusWithFinalEffect).getBaseValue(member.stats, member)
        }
        throw UnsupportedOperationException("MutableStats and certain StatBonus's are the only stats supported by stat conditions. Others must be implemented as actual conditions. The stat was [%s]".format(stat.key))
    }
}