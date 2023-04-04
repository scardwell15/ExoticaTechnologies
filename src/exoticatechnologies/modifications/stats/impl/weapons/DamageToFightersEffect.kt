package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.*
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class DamageToFightersEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "damageToFighters"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.damageToFighters
    }
}