package exoticatechnologies.modifications.stats.impl.shield

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class ShieldTurnRateEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "shieldTurnRate"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.shieldTurnRateMult
    }

    override fun shouldHide(member: FleetMemberAPI): Boolean {
        if (member.hullSpec.shieldSpec == null) {
            return true
        }
        return false
    }
}