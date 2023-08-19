package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class EnergyFireRateEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "energyFireRate"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.energyRoFMult
    }
}