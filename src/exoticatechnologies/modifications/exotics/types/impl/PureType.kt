package exoticatechnologies.modifications.exotics.types.impl

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.StringUtils
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class PureType : ExoticType("PURE", colorOverlay = Color(255, 200, 125, 180), sprite = "graphics/icons/overlays/pure.png") {
    override fun getPositiveMult(member: FleetMemberAPI, mods: ShipModifications): Float {
        return if (isModsEmpty(mods))
            2.5f
        else 1.25f
    }

    override fun getNegativeMult(member: FleetMemberAPI, mods: ShipModifications): Float {
        return if (isModsEmpty(mods))
            1f
        else 1.5f
    }

    private fun isModsEmpty(mods: ShipModifications): Boolean {
        return mods.getExoticIdSet().size == 1 && mods.getUpgradeMap().isEmpty();
    }

    override fun getChanceMult(context: ShipModFactory.GenerationContext): Float {
        if (context.mods.hasExotics()) {
            return 0.05f
        }
        return 1f
    }

    override fun mutateGenerationContext(context: ShipModFactory.GenerationContext) {
        context.upgradeChanceMult *= 0.001f
        context.exoticChanceMult *= 0.001f
    }

    override fun getNameTranslation(member: FleetMemberAPI, mods: ShipModifications): StringUtils.Translation? {
        return StringUtils.getTranslation("ExoticTypes", "TooltipTextCondition")
            .formatFloat("positiveMult", getPositiveMult(member, mods))
            .formatFloat("negativeMult", getNegativeMult(member, mods))
            .format("condition", StringUtils.getString("ExoticTypes", "PureCondition"), colorOverlay.setAlpha(255))
    }

    override fun getItemDescTranslation(): StringUtils.Translation? {
        return StringUtils.getTranslation("ExoticTypes", "PureItemDescription")
    }
}