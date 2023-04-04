package exoticatechnologies.modifications.stats

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.StringUtils.Translation
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.optFloat

abstract class UpgradeModEffect : ModEffect<Upgrade>() {
    open var baseEffect: Float = 0f
    open var scalingEffect: Float = 0f
    open var flat: Boolean = false
    open var negativeIsBuff: Boolean = false
    open var positiveAlsoMult: Boolean = false
    open var startingLevel: Int = 1
    open var hidden: Boolean = false
    open var appliesToFighters: Boolean = false
    open var hullmodShowsFinalValue: Boolean = true
        get() = !appliesToFighters && field

    override fun getName(): String {
        if (appliesToFighters) {
            return StringUtils.getTranslation("ModEffects","FighterStatName")
                .format("stat", Misc.lcFirst(super.getName()))
                .toStringNoFormats()
        }
        return super.getName()
    }

    open fun setup(obj: JSONObject) {
        baseEffect = obj.optFloat("baseEffect", 0f)
        scalingEffect = obj.optFloat("scalingEffect", 0f)
        startingLevel = obj.optInt("startingLevel", 1).coerceAtLeast(1)
        hidden = obj.optBoolean("hidden", false)
        appliesToFighters = obj.optBoolean("appliesToFighters", false)
        flat = obj.optBoolean("flat", false)
    }

    fun getRatio(member: FleetMemberAPI, mods: ShipModifications, mod: Upgrade): Float {
        if (mods.getUpgrade(mod) >= startingLevel) {
            val effectiveLevel: Float = (mods.getUpgrade(mod) - (startingLevel - 1)).toFloat()
            val effectiveMax: Float = (mod.maxLevel - (startingLevel - 1)).toFloat()
            return effectiveLevel / effectiveMax
        }
        return 0f
    }

    open fun getCurrentEffect(member: FleetMemberAPI, mods: ShipModifications, mod: Upgrade): Float {
        val effect = baseEffect + scalingEffect * getRatio(member, mods, mod)
        if (handleAsMult()) {
            return 1 + (effect / 100)
        } else if (flat) {
            return effect
        } else {
            return effect / 100f
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
        val effect = scalingEffect / mod.maxLevel
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
        return !flat && (positiveAlsoMult || scalingEffect < 0)
    }

    open fun shouldHide(member: FleetMemberAPI): Boolean {
        return false
    }

    open fun getStatTranslationParent(): String {
        return "ModEffects"
    }

    open fun getStatTranslationForTooltipBenefit(showFinal: Boolean): Translation {
        if (showFinal) {
            return StringUtils.getTranslation(getStatTranslationParent(), "StatBenefitWithFinal")
        } else {
            return StringUtils.getTranslation(getStatTranslationParent(), "StatBenefit")
        }
    }

    open fun getStatTranslationForTooltipMalus(showFinal: Boolean): Translation {
        if (showFinal) {
            return StringUtils.getTranslation(getStatTranslationParent(), "StatMalusWithFinal")
        } else {
            return StringUtils.getTranslation(getStatTranslationParent(), "StatMalus")
        }
    }

    open fun getStatTranslationForShopBenefit(hidePerLevel: Boolean): Translation {
        if (hidePerLevel) {
            return StringUtils.getTranslation(getStatTranslationParent(), "StatBenefitInShopMaxed")
        } else {
            return StringUtils.getTranslation(getStatTranslationParent(), "StatBenefitInShop")
        }
    }

    open fun getStatTranslationForShopMalus(hidePerLevel: Boolean): Translation {
        if (hidePerLevel) {
            return StringUtils.getTranslation(getStatTranslationParent(), "StatMalusInShopMaxed")
        } else {
            return StringUtils.getTranslation(getStatTranslationParent(), "StatMalusInShop")
        }
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
            return addMultMalusToTooltip(tooltip, stats, member, mods, mod)
        } else if (flat) {
            if (scalingEffect > 0 || negativeIsBuff) {
                return addFlatBenefitToTooltip(tooltip, stats, member, mods, mod)
            }
            return addFlatMalusToTooltip(tooltip, stats, member, mods, mod)
        } else {
            if ((negativeIsBuff && scalingEffect < 0) || (!negativeIsBuff && scalingEffect > 0)) {
                return addPercentBenefitToTooltip(tooltip, stats, member, mods, mod)
            } else {
                return addPercentMalusToTooltip(tooltip, stats, member, mods, mod)
            }
        }
    }

    fun addPercentBenefitToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForTooltipBenefit(hullmodShowsFinalValue)
            .format("stat", getName())
            .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod) * 100f)
            .formatWithOneDecimalAndModifier("finalValue", getEffectiveValue(stats, member, mods, mod))
            .addToTooltip(tooltip)
    }

    fun addPercentMalusToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForTooltipMalus(hullmodShowsFinalValue)
            .format("stat", getName())
            .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod) * 100f)
            .formatWithOneDecimalAndModifier("finalValue", getEffectiveValue(stats, member, mods, mod))
            .addToTooltip(tooltip)
    }

    fun addFlatBenefitToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForTooltipBenefit(hullmodShowsFinalValue)
            .format("stat", getName())
            .formatWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
            .formatWithOneDecimalAndModifier("finalValue", getEffectiveValue(stats, member, mods, mod))
            .addToTooltip(tooltip)
    }

    fun addFlatMalusToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForTooltipMalus(hullmodShowsFinalValue)
            .format("stat", getName())
            .formatWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
            .formatWithOneDecimalAndModifier("finalValue", getEffectiveValue(stats, member, mods, mod))
            .addToTooltip(tooltip)
    }

    open fun addMultBenefitToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForTooltipBenefit(hullmodShowsFinalValue)
            .format("stat", getName())
            .formatMult("percent", getCurrentEffect(member, mods, mod))
            .formatWithOneDecimalAndModifier("finalValue", getEffectiveValue(stats, member, mods, mod))
            .addToTooltip(tooltip)
    }

    open fun addMultMalusToTooltip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForTooltipMalus(hullmodShowsFinalValue)
            .format("stat", getName())
            .formatMult("percent", getCurrentEffect(member, mods, mod))
            .formatWithOneDecimalAndModifier("finalValue", getEffectiveValue(stats, member, mods, mod))
            .addToTooltip(tooltip)
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
        } else if (flat) {
            if (scalingEffect > 0 || negativeIsBuff) {
                return addFlatBenefitToShop(tooltip, member, mods, mod)
            } else {
                return addFlatMalusToShop(tooltip, member, mods, mod)
            }
        } else {
            if ((negativeIsBuff && scalingEffect < 0) || (!negativeIsBuff && scalingEffect > 0)) {
                return addPercentBenefitToShop(tooltip, member, mods, mod)
            } else {
                return addPercentDrawbackToShop(tooltip, member, mods, mod)
            }
        }
    }

    fun addFlatBenefitToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForShopBenefit(mods.isMaxLevel(member, mod))
            .format("stat", getName())
            .formatWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
            .formatWithOneDecimalAndModifier("perLevel", getPerLevelEffect(member, mods, mod))
            .formatWithOneDecimalAndModifier("finalValue", getFinalEffect(member, mods, mod))
            .addToTooltip(tooltip)
    }

    fun addFlatMalusToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForShopMalus(mods.isMaxLevel(member, mod))
            .format("stat", getName())
            .formatWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod))
            .formatWithOneDecimalAndModifier("perLevel", getPerLevelEffect(member, mods, mod))
            .formatWithOneDecimalAndModifier("finalValue", getFinalEffect(member, mods, mod))
            .addToTooltip(tooltip)
    }

    fun addPercentBenefitToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForShopBenefit(mods.isMaxLevel(member, mod))
            .format("stat", getName())
            .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod) * 100f)
            .formatPercWithOneDecimalAndModifier("perLevel", getPerLevelEffect(member, mods, mod))
            .formatPercWithOneDecimalAndModifier("finalValue", getFinalEffect(member, mods, mod))
            .addToTooltip(tooltip)
    }

    fun addPercentDrawbackToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForShopMalus(mods.isMaxLevel(member, mod))
            .format("stat", getName())
            .formatPercWithOneDecimalAndModifier("percent", getCurrentEffect(member, mods, mod) * 100f)
            .formatPercWithOneDecimalAndModifier("perLevel", getPerLevelEffect(member, mods, mod))
            .formatPercWithOneDecimalAndModifier("finalValue", getFinalEffect(member, mods, mod))
            .addToTooltip(tooltip)
    }

    fun addMultBenefitToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForShopBenefit(mods.isMaxLevel(member, mod))
            .format("stat", getName())
            .formatMult("percent", getCurrentEffect(member, mods, mod))
            .formatMultWithModifier("perLevel", getPerLevelEffect(member, mods, mod))
            .formatMult("finalValue", getFinalEffect(member, mods, mod))
            .addToTooltip(tooltip)
    }

    fun addMultDrawbackToShop(
        tooltip: TooltipMakerAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        mod: Upgrade
    ): LabelAPI {
        return getStatTranslationForShopMalus(mods.isMaxLevel(member, mod))
            .format("stat", getName())
            .formatMult("percent", getCurrentEffect(member, mods, mod))
            .formatMultWithModifier("perLevel", getPerLevelEffect(member, mods, mod))
            .formatMult("finalValue", getFinalEffect(member, mods, mod))
            .addToTooltip(tooltip)
    }
}