package exoticatechnologies.modifications.stats.impl.engines

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import exoticatechnologies.modifications.stats.UpgradeStatBonusEffect

class EngineHealthEffect : UpgradeStatBonusEffect() {
    override val key: String
        get() = "engineHealth"

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.engineHealthBonus
    }
}