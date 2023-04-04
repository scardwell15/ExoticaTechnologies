package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.*
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class DamageToMissilesEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "damageToMissiles"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.damageToMissiles
    }
}