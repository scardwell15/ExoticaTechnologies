package exoticatechnologies.modifications.stats.impl.shield

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.stats.UpgradeStatBonusWithFinalEffect

class ShieldArcEffect : UpgradeStatBonusWithFinalEffect() {
    override val key: String
        get() = "shieldArc"

    override fun getBaseValue(stats: MutableShipStatsAPI, member: FleetMemberAPI): Float {
        return member.hullSpec.shieldSpec?.arc ?: 0f
    }

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.shieldArcBonus
    }

    override fun shouldHide(member: FleetMemberAPI): Boolean {
        if (member.hullSpec.shieldSpec == null) {
            return true
        }
        return false
    }
}