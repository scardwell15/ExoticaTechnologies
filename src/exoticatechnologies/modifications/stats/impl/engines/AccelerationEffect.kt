package exoticatechnologies.modifications.stats.impl.engines

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class AccelerationEffect : UpgradeMutableStatEffect() {
    override val key: String
        get() = "acceleration"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.acceleration
    }
}