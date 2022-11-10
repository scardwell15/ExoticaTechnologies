package exoticatechnologies.modifications.stats.impl.engines

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class DecelerationEffect : UpgradeMutableStatEffect() {
    override val key: String
        get() = "deceleration"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.deceleration
    }
}