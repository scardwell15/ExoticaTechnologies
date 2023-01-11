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
        return getCurrentEffect(member, mods, mod)
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