package exoticatechnologies.modifications.stats.impl.health

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.stats.UpgradeStatBonusWithFinalEffect

class ArmorEffect : UpgradeStatBonusWithFinalEffect() {
    override val key: String
        get() = "armor"

    override fun getBaseValue(stats: MutableShipStatsAPI, member: FleetMemberAPI): Float {
        return member.hullSpec.armorRating
    }

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.armorBonus
    }
}