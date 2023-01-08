package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import exoticatechnologies.modifications.stats.UpgradeStatBonusEffect

class MissileHealthEffect : UpgradeStatBonusEffect() {
    override val key: String
        get() = "missileHealth"

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.missileHealthBonus
    }
}