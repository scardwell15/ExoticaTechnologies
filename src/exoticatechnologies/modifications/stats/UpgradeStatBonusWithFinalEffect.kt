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
            return getStat(stats).getMultBonus(mod.key).getValue() * getBaseValue(stats, member)
        }
        return getStat(stats).getPercentBonus(mod.key).getValue() / 100f * getBaseValue(stats, member)
    }
}