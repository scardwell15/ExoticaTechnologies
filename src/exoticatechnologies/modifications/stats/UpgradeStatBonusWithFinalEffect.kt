package exoticatechnologies.modifications.stats

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade

abstract class UpgradeStatBonusWithFinalEffect : UpgradeStatBonusEffect() {
    override var hullmodShowsFinalValue = true

    abstract fun getBaseValue(stats: MutableShipStatsAPI, member: FleetMemberAPI): Float

    override fun getEffectiveValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        if (handleAsMult()) {
            val base = getBaseValue(stats, member)
            val currEffect = getCurrentEffect(member, mods, mod)
            val final = base * -(1 - currEffect)
            return final
        } else if (flat) {
            return getCurrentEffect(member, mods, mod)
        } else {
            val base = getBaseValue(stats, member)
            val currEffect = getCurrentEffect(member, mods, mod)
            val final = base * currEffect
            return final
        }
    }
}