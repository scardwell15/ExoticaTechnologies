package exoticatechnologies.modifications.stats.impl.engines

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect
import exoticatechnologies.modifications.upgrades.Upgrade

class TurnRateEffect : UpgradeMutableStatEffect() {
    override var hullmodShowsFinalValue: Boolean = false

    override val key: String
        get() = "turnRate"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.maxTurnRate
    }

    override fun applyToStats(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ) {
        applyEffectToStat(stats.maxTurnRate, member, mods, mod)
        applyEffectToStat(stats.turnAcceleration, member, mods, mod)
    }
}