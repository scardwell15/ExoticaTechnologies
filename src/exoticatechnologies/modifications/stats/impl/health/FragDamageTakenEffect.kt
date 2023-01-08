package exoticatechnologies.modifications.stats.impl.health

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class FragDamageTakenEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "fragDamageTaken"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.fragmentationDamageTakenMult
    }
}