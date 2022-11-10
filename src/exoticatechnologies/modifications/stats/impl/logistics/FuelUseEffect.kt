package exoticatechnologies.modifications.stats.impl.logistics

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.stats.UpgradeStatBonusWithFinalEffect

class FuelUseEffect : UpgradeStatBonusWithFinalEffect() {
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "fuelUse"

    override fun getBaseValue(stats: MutableShipStatsAPI, member: FleetMemberAPI): Float {
        return member.hullSpec.fuelPerLY
    }

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.fuelUseMod
    }
}