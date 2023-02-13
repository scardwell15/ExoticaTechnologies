package exoticatechnologies.modifications.stats.impl.engines

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect
import exoticatechnologies.modifications.upgrades.Upgrade

class BurnLevelEffect : UpgradeMutableStatEffect() {
    override val key: String
        get() = "burnLevel"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.maxBurnLevel
    }

    override fun getFinalEffect(member: FleetMemberAPI, mods: ShipModifications, mod: Upgrade): Float {
        val effect = baseEffect + scalingEffect
        if (handleAsMult()) {
            return member.stats.maxBurnLevel.modifiedValue * (1 + (effect / 100))
        } else {
            return member.stats.maxBurnLevel.modifiedValue * effect / 100
        }
    }
}