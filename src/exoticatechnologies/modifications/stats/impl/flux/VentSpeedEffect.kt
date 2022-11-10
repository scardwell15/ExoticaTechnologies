package exoticatechnologies.modifications.stats.impl.flux

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class VentSpeedEffect : UpgradeMutableStatEffect() {
    override val key: String
        get() = "ventSpeed"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.ventRateMult
    }
}