package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import exoticatechnologies.modifications.stats.UpgradeStatBonusEffect

class MissileRangeEffect : UpgradeStatBonusEffect() {
    override val key: String
        get() = "missileRange"

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.missileWeaponRangeBonus
    }
}