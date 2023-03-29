package exoticatechnologies.modifications.stats.impl.fighters

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.impl.campaign.ids.Stats
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class ReplacementRateDegenEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false
    override var negativeIsBuff: Boolean = true
    
    override val key: String
        get() = "replacementRateDegen"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.dynamic.getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT)
    }
}