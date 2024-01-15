package exoticatechnologies.modifications.exotics.types.impl

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.StringUtils
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class PureType :
    ExoticType("PURE", colorOverlay = Color(255, 200, 125, 255), sprite = "graphics/icons/overlays/pure.png") {
    override fun getPositiveMult(member: FleetMemberAPI, mods: ShipModifications): Float {
        return 2.5f - 1.5f * getBandwidthRatio(member, mods) - 0.75f * getExoticRatio(member, mods)
    }

    override fun getNegativeMult(member: FleetMemberAPI, mods: ShipModifications): Float {
        return 1f + 0.5f * getBandwidthRatio(member, mods) + 0.25f * getExoticRatio(member, mods)
    }

    private fun getBandwidthRatio(member: FleetMemberAPI, mods: ShipModifications): Float {
        return ((mods.getUsedBandwidth() / mods.getBaseBandwidth(member))).coerceIn(0f..1f)
    }

    private fun getExoticRatio(member: FleetMemberAPI, mods: ShipModifications): Float {
        return ((mods.getExoticSet().size - 1f) / (mods.getMaxExotics(member) - 1f)).coerceIn(0f..1f)
    }

    override fun getChanceMult(context: ShipModFactory.GenerationContext): Float {
        if (context.mods.hasExotics()) {
            return 0.1f
        }
        return 1f
    }

    override fun mutateGenerationContext(context: ShipModFactory.GenerationContext) {
        context.upgradeChanceMult *= 0.1f
        context.exoticChanceMult *= 0.01f
    }

    override fun getDescriptionTranslation(member: FleetMemberAPI, mods: ShipModifications): StringUtils.Translation? {
        return StringUtils.getTranslation("ExoticTypes", "TooltipTextCondition")
            .formatFloat("positiveMult", getPositiveMult(member, mods))
            .formatFloat("negativeMult", getNegativeMult(member, mods))
            .format("condition", StringUtils.getString("ExoticTypes", "PureCondition"), colorOverlay.setAlpha(255))
    }

    override fun getItemDescTranslation(): StringUtils.Translation? {
        return StringUtils.getTranslation("ExoticTypes", "PureItemDescription")
    }
}