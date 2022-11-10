package exoticatechnologies.modifications.stats.impl.cr

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.stats.UpgradeStatBonusWithFinalEffect

class PeakPerformanceTimeEffect : UpgradeStatBonusWithFinalEffect() {
    override val key: String
        get() = "peakPerformanceTime"

    override fun getBaseValue(stats: MutableShipStatsAPI, member: FleetMemberAPI): Float {
        return member.hullSpec.noCRLossTime
    }

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.peakCRDuration
    }
}