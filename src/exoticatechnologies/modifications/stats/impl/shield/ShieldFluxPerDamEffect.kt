package exoticatechnologies.modifications.stats.impl.shield

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.stats.UpgradeMutableStatEffect
import exoticatechnologies.modifications.upgrades.Upgrade

class ShieldFluxPerDamEffect : UpgradeMutableStatEffect() {
    override var negativeIsBuff: Boolean = true

    override val key: String
        get() = "shieldFluxDam"

    override fun getStat(stats: MutableShipStatsAPI): MutableStat {
        return stats.shieldDamageTakenMult
    }

    override fun getBaseValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float {
        return stats.variant.hullSpec.shieldSpec.fluxPerDamageAbsorbed
    }

    override fun shouldHide(member: FleetMemberAPI): Boolean {
        if (member.hullSpec.shieldSpec == null) {
            return true
        }
        return false
    }

    override fun addMultBenefitToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForTooltipBenefit(hullmodShowsFinalValue)
            .format("stat", getName())
            .formatMult("percent", getCurrentEffect(member, mods, mod))
            .formatFloatWithModifier("finalValue", getEffectiveValue(stats, member, mods, mod))
            .addToTooltip(tooltip)
    }

    override fun addMultMalusToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForTooltipMalus(hullmodShowsFinalValue)
            .format("stat", getName())
            .formatMult("percent", getCurrentEffect(member, mods, mod))
            .formatFloatWithModifier("finalValue", getEffectiveValue(stats, member, mods, mod))
            .addToTooltip(tooltip)
    }
}