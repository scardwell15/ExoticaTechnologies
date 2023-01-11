package exoticatechnologies.modifications.stats

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.impl.shield.ShieldFluxPerDamEffect
import exoticatechnologies.modifications.upgrades.Upgrade

abstract class UpgradeMutableStatEffect : UpgradeModEffect() {
    abstract fun getStat(stats: MutableShipStatsAPI): MutableStat

    override fun getEffectiveValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        if (handleAsMult()) {
            val base = getBaseValue(stats, member, mods, mod)
            val currEffect = getCurrentEffect(member, mods, mod)
            val final = base * -(1 - currEffect)
            return final
        } else if (flat) {
            return getCurrentEffect(member, mods, mod)
        } else {
            val base = getBaseValue(stats, member, mods, mod)
            val currEffect = getCurrentEffect(member, mods, mod)
            val final = base * currEffect
            return final
        }
    }

    open fun getBaseValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        return getStat(stats).baseValue
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
        val currEffect = getCurrentEffect(member, mods, mod)


        if (handleAsMult()) {
            stat.modifyMult(mod.key, currEffect)
        } else if (flat) {
            stat.modifyFlat(mod.key, currEffect)
        } else {
            stat.modifyPercent(mod.key, currEffect * 100f)
        }
    }
}