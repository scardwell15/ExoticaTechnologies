package exoticatechnologies.modifications.stats

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade

abstract class UpgradeMutableStatEffect : UpgradeModEffect() {
    abstract fun getStat(stats: MutableShipStatsAPI): MutableStat

    override fun getEffectiveValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        return getCurrentEffect(member, mods, mod) * getStat(stats).baseValue
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
        stat: MutableStat,
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