package exoticatechnologies.modifications.stats.impl.cr

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.stats.UpgradeStatBonusWithFinalEffect

class CRToDeployEffect : UpgradeStatBonusWithFinalEffect() {
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "crToDeploy"

    override fun getBaseValue(stats: MutableShipStatsAPI, member: FleetMemberAPI): Float {
        return member.hullSpec.crToDeploy
    }

    override fun getStat(stats: MutableShipStatsAPI): StatBonus {
        return stats.crPerDeploymentPercent
    }
}