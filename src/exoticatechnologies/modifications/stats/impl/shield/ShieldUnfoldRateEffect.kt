package exoticatechnologies.modifications.stats.impl.shield

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class ShieldUnfoldRateEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "shieldUnfoldRate"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.shieldUnfoldRateMult
    }

    override fun shouldHide(member: FleetMemberAPI): Boolean {
        if (member.hullSpec.shieldSpec == null) {
            return true
        }
        return false
    }
}