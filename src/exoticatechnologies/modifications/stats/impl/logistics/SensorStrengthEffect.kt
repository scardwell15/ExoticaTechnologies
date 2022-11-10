package exoticatechnologies.modifications.stats.impl.logistics

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class SensorStrengthEffect : UpgradeMutableStatEffect() {
    override val key: String
        get() = "sensorStrength"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.sensorStrength
    }
}