package exoticatechnologies.modifications.stats.impl.weapons

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import exoticatechnologies.modifications.stats.UpgradeStatBonusEffect

class EnergyMagazineEffect : UpgradeStatBonusEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "energyMagazines"

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.energyAmmoBonus
    }
}