package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class MaxRecoilEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "maxRecoil"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.maxRecoilMult
    }
}