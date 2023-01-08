package exoticatechnologies.modifications.stats.impl.fighters

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import exoticatechnologies.modifications.stats.UpgradeStatBonusEffect

class FighterRangeEffect : UpgradeStatBonusEffect() {
    override val key: String
        get() = "fighterWingRange"

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.fighterWingRange
    }
}