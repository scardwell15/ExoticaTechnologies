package exoticatechnologies.modifications.stats

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade

abstract class UpgradeStatBonusWithFinalEffect : UpgradeStatBonusEffect() {
    abstract fun getBaseValue(stats: MutableShipStatsAPI, member: FleetMemberAPI): Float

    override fun getEffectiveValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        if (handleAsMult()) {
            return getCurrentEffect(member, mods, mod) * getBaseValue(stats, member)
        }
        return getCurrentEffect(member, mods, mod) / 100f * getBaseValue(stats, member)
    }
}