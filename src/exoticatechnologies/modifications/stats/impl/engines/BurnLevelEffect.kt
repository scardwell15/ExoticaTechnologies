package exoticatechnologies.modifications.stats.impl.engines

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class BurnLevelEffect : UpgradeMutableStatEffect() {
    override val key: String
        get() = "burnLevel"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.maxBurnLevel
    }
}