package exoticatechnologies.modifications.stats.impl.shield

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class ShieldFluxPerDamEffect : UpgradeMutableStatEffect() {
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "shieldFluxDam"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.shieldDamageTakenMult
    }

    override fun shouldHide(member: FleetMemberAPI): Boolean {
        if (member.hullSpec.shieldSpec == null) {
            return true
        }
        return false
    }
}