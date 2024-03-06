package exoticatechnologies.modifications.stats.impl.flux

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect

class FluxDissipationEffect : UpgradeMutableStatEffect() {
    override val key: String
        get() = "fluxDissipation"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.fluxDissipation
    }
}