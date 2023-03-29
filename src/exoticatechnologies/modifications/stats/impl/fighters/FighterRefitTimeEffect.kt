package exoticatechnologies.modifications.stats.impl.fighters

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class FighterRefitTimeEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "fighterRefitTime"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.fighterRefitTimeMult
    }
}