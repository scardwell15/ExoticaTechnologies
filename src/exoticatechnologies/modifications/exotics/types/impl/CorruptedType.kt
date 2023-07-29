package exoticatechnologies.modifications.exotics.types.impl

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.StringUtils
import java.awt.Color


class CorruptedType : ExoticType("CORRUPTED", colorOverlay = Color(255, 0, 0, 160), sprite = "graphics/icons/overlays/corrupted.png") {
    override fun getPositiveMult(member: FleetMemberAPI, mods: ShipModifications): Float {
        return 2f
    }

    override fun getNegativeMult(member: FleetMemberAPI, mods: ShipModifications): Float {
        return 1.75f
    }

    override fun getDescriptionTranslation(member: FleetMemberAPI, mods: ShipModifications): StringUtils.Translation? {
        return StringUtils.getTranslation("ExoticTypes", "TooltipText")
            .formatFloat("positiveMult", getPositiveMult(member, mods))
            .formatFloat("negativeMult", getNegativeMult(member, mods))
    }


    override fun getItemDescTranslation(): StringUtils.Translation? {
        return StringUtils.getTranslation("ExoticTypes", "CorruptedItemDescription")
    }
}