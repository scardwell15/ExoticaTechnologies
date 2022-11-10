package exoticatechnologies.modifications.stats.impl.logistics

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.stats.UpgradeStatBonusWithFinalEffect

class MinimumCrewEffect : UpgradeStatBonusWithFinalEffect() {
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "minCrew"

    override fun getBaseValue(stats: MutableShipStatsAPI, member: FleetMemberAPI): Float {
        return member.hullSpec.minCrew
    }

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.minCrewMod
    }
}