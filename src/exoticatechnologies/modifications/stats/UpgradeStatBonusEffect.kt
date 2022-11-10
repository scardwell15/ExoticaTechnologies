package exoticatechnologies.modifications.stats

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade

abstract class UpgradeStatBonusEffect : UpgradeModEffect() {
    override var hullmodShowsFinalValue = false

    abstract fun getStat(stats: MutableShipStatsAPI): StatBonus

    override fun getEffectiveValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        if (handleAsMult()) {
            return getStat(stats).getMultBonus(mod.key).getValue()
        }
        return getStat(stats).getPercentBonus(mod.key).getValue()
    }

    override fun applyToStats(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ) {
        applyEffectToStat(getStat(stats), member, mods, mod)
    }

    fun applyEffectToStat(
        stat: StatBonus,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ) {
        if (handleAsMult()) {
            stat.modifyMult(mod.key, getCurrentEffect(member, mods, mod))
        } else {
            stat.modifyPercent(mod.key, getCurrentEffect(member, mods, mod))
        }
    }
}