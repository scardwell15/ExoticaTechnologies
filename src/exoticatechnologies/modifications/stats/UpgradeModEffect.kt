package exoticatechnologies.modifications.stats

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils

abstract class UpgradeModEffect : ModEffect<Upgrade>() {
    open var baseEffect: Float = 0f
    open var scalingEffect: Float = 0f
    open var negativeIsBuff: Boolean = false
    open var positiveAlsoMult: Boolean = false
    open var startingLevel: Int = 1
    open var hullmodShowsFinalValue: Boolean = true
    open var hidden: Boolean = false

    fun getRatio(member: FleetMemberAPI, mods: ShipModifications, mod: Upgrade): Float {
        if (mods.getUpgrade(mod) >= startingLevel) {
            val effectiveLevel: Float = (mods.getUpgrade(mod) - (startingLevel - 1)).toFloat()
            val effectiveMax: Float = (mod.getMaxLevel(member) - (startingLevel - 1)).toFloat()
            return effectiveLevel / effectiveMax
        }
        return 0f
    }

    open fun getBaseValue(member: FleetMemberAPI) {

    }

    open fun getCurrentEffect(member: FleetMemberAPI, mods: ShipModifications, mod: Upgrade): Float {
        val effect = baseEffect + scalingEffect * getRatio(member, mods, mod)
        if (handleAsMult()) {
            return 1 + (effect / 100)
        } else {
            return effect
        }
    }

    open fun getFinalEffect(member: FleetMemberAPI, mods: ShipModifications, mod: Upgrade): Float {
        val effect = baseEffect + scalingEffect
        if (handleAsMult()) {
            return 1 + (effect / 100)
        } else {
            return effect
        }
    }

    open fun getPerLevelEffect(member: FleetMemberAPI, mods: ShipModifications, mod: Upgrade): Float {
        val effect = scalingEffect / mod.getMaxLevel(member)
        if (handleAsMult()) {
            return effect / 100
        } else {
            return effect
        }
    }

    abstract fun getEffectiveValue(
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): Float

    open fun handleAsMult(): Boolean {
        return positiveAlsoMult || scalingEffect < 0
    }

    open fun shouldHide(member: FleetMemberAPI): Boolean {
        return false
    }

    override fun printToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI? {
        if (shouldHide(member) || hidden || mods.getUpgrade(mod) < startingLevel) {
            return null
        }

        if (handleAsMult()) {
            if ((scalingEffect > 0 && positiveAlsoMult) || (scalingEffect < 0 && negativeIsBuff)) {
                return addMultBenefitToTooltip(tooltip, stats, member, mods, mod)
            }
            return addMultDrawbackToTooltip(tooltip, stats, member, mods, mod)
        }
        return addPercentBenefitToTooltip(tooltip, stats, member, mods, mod)
    }

    fun addPercentBenefitToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        if (hullmodShowsFinalValue) {
            return StringUtils.getTranslation("ModEffects", "StatBenefitWithFinal")
                .format("stat", getName())
                .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
                .formatWithOneDecimalAndModifier("finalValue", getEffectiveValue(stats, member, mods, mod))
                .addToTooltip(tooltip)
        } else {
            return StringUtils.getTranslation("ModEffects", "StatBenefit")
                .format("stat", getName())
                .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
                .addToTooltip(tooltip)
        }
    }

    fun addMultBenefitToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        if (hullmodShowsFinalValue) {
            return StringUtils.getTranslation("ModEffects", "StatBenefitWithFinal")
                .format("stat", getName())
                .formatMult("percent", getCurrentEffect(member, mods, mod))
                .formatMult("finalValue", getEffectiveValue(stats, member, mods, mod))
                .addToTooltip(tooltip)
        } else {
            return StringUtils.getTranslation("ModEffects", "StatBenefit")
                .format("stat", getName())
                .formatMult("percent", getCurrentEffect(member, mods, mod))
                .addToTooltip(tooltip)
        }
    }

    fun addMultDrawbackToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        if (mods.isMaxLevel(member, mod)) {
            return StringUtils.getTranslation("ModEffects", "StatMalusWithFinal")
                .format("stat", getName())
                .formatMult("percent", getCurrentEffect(member, mods, mod))
                .formatMult("finalValue", getEffectiveValue(stats, member, mods, mod))
                .addToTooltip(tooltip)
        } else {
            return StringUtils.getTranslation("ModEffects", "StatMalus")
                .format("stat", getName())
                .formatMult("percent", getCurrentEffect(member, mods, mod))
                .addToTooltip(tooltip)
        }
    }

    override fun printToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI? {
        if (shouldHide(member) || hidden) {
            return null
        }

        if (handleAsMult()) {
            if (scalingEffect > 0 || negativeIsBuff) {
                return addMultBenefitToShop(tooltip, member, mods, mod)
            } else {
                return addMultDrawbackToShop(tooltip, member, mods, mod)
            }
        } else {
            if ((negativeIsBuff && scalingEffect < 0) || (!negativeIsBuff && scalingEffect > 0)) {
                return addPercentBenefitToShop(tooltip, member, mods, mod)
            } else {
                return addPercentDrawbackToShop(tooltip, member, mods, mod)
            }
        }
    }

    fun addPercentBenefitToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        if (mods.isMaxLevel(member, mod)) {
            return StringUtils.getTranslation("ModEffects", "StatBenefitInShopMaxed")
                .format("stat", getName())
                .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
                .addToTooltip(tooltip)
        } else {
            return StringUtils.getTranslation("ModEffects", "StatBenefitInShop")
                .format("stat", getName())
                .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
                .formatPercWithOneDecimalAndModifier("perLevel", getPerLevelEffect(member, mods, mod))
                .formatPercWithOneDecimalAndModifier("finalValue", getFinalEffect(member, mods, mod))
                .addToTooltip(tooltip)
        }
    }

    fun addPercentDrawbackToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        val effect = getCurrentEffect(member, mods, mod)
        if (mods.isMaxLevel(member, mod)) {
            return StringUtils.getTranslation("ModEffects", "StatMalusInShopMaxed")
                .format("stat", getName())
                .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
                .addToTooltip(tooltip)
        } else {
            return StringUtils.getTranslation("ModEffects", "StatMalusInShop")
                .format("stat", getName())
                .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
                .formatPercWithOneDecimalAndModifier("perLevel", getPerLevelEffect(member, mods, mod))
                .formatPercWithOneDecimalAndModifier("finalValue", getFinalEffect(member, mods, mod))
                .addToTooltip(tooltip)
        }
    }

    fun addMultBenefitToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        if (mods.isMaxLevel(member, mod)) {
            return StringUtils.getTranslation("ModEffects", "StatBenefitInShopMaxed")
                .format("stat", getName())
                .formatMult("percent", getFinalEffect(member, mods, mod))
                .addToTooltip(tooltip)
        } else {
            return StringUtils.getTranslation("ModEffects", "StatBenefitInShop")
                .format("stat", getName())
                .formatMult("percent", getCurrentEffect(member, mods, mod))
                .formatMultWithModifier("perLevel", getPerLevelEffect(member, mods, mod))
                .formatMult("finalValue", getFinalEffect(member, mods, mod))
                .addToTooltip(tooltip)
        }
    }

    fun addMultDrawbackToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        if (mods.isMaxLevel(member, mod)) {
            return StringUtils.getTranslation("ModEffects", "StatMalusInShopMaxed")
                .format("stat", getName())
                .formatMult("percent", getFinalEffect(member, mods, mod))
                .addToTooltip(tooltip)
        } else {
            return StringUtils.getTranslation("ModEffects", "StatMalusInShop")
                .format("stat", getName())
                .formatMult("percent", getCurrentEffect(member, mods, mod))
                .formatMultWithModifier("perLevel", getPerLevelEffect(member, mods, mod))
                .formatMult("finalValue", getFinalEffect(member, mods, mod))
                .addToTooltip(tooltip)
        }
    }
}