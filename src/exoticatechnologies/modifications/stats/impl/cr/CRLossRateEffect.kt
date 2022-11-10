package exoticatechnologies.modifications.stats.impl.cr

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import exoticatechnologies.modifications.stats.UpgradeStatBonusEffect

class CRLossRateEffect : UpgradeStatBonusEffect() {
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "crLossRate"

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.crLossPerSecondPercent
    }
}