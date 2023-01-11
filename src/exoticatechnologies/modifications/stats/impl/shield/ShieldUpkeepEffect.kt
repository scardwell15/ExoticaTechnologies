package exoticatechnologies.modifications.stats.impl.shield

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect
import exoticatechnologies.modifications.upgrades.Upgrade

class ShieldUpkeepEffect : UpgradeMutableStatEffect() {
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "shieldUpkeep"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.shieldUpkeepMult
    }

    override fun getBaseValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        return stats.variant.hullSpec.shieldSpec.upkeepCost
    }

    override fun shouldHide(member: FleetMemberAPI): Boolean {
        if (member.hullSpec.shieldSpec == null) {
            return true
        }
        return false
    }
}