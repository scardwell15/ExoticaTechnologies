package exoticatechnologies.modifications.stats.impl.flux

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class VentSpeedEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "ventSpeed"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.ventRateMult
    }
}