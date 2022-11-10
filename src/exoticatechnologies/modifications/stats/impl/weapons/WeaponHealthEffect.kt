package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import exoticatechnologies.modifications.stats.UpgradeStatBonusEffect

class WeaponHealthEffect : UpgradeStatBonusEffect() {
    override val key: String
        get() = "weaponHealth"

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.weaponHealthBonus
    }
}