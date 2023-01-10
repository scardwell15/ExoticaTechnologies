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
        val currEffect = getCurrentEffect(member, mods, mod)
        val base = getBaseValue(stats, member)
        return currEffect * base
    }
}