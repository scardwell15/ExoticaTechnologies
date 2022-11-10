package exoticatechnologies.modifications.stats.impl.cr

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class CRRecoveryRateEffect : UpgradeMutableStatEffect() {
    override val key: String
        get() = "crRecoveryRate"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.baseCRRecoveryRatePercentPerDay
    }
}