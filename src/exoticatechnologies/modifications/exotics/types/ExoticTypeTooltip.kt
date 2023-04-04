package exoticatechnologies.modifications.exotics.types

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.BaseTooltipCreator
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications

class ExoticTypeTooltip(
    private val tooltip: TooltipMakerAPI,
    val member: FleetMemberAPI,
    val mods: ShipModifications,
    val type: ExoticType
) : BaseTooltipCreator() {
    companion object {
        fun addToPrev(
            tooltip: TooltipMakerAPI,
            member: FleetMemberAPI,
            mods: ShipModifications,
            type: ExoticType,
            loc: TooltipMakerAPI.TooltipLocation
        ) {
            tooltip.addTooltipToPrevious(ExoticTypeTooltip(tooltip, member, mods, type), loc)
        }

        fun addToPrev(tooltip: TooltipMakerAPI, member: FleetMemberAPI, mods: ShipModifications, type: ExoticType) {
            addToPrev(tooltip, member, mods, type, TooltipMakerAPI.TooltipLocation.BELOW)
        }
    }

    override fun getTooltipWidth(tooltipParam: Any): Float {
        type.getDescriptionTranslation(member, mods)?.let {
            return tooltip.computeStringWidth(it.toStringNoFormats()).coerceAtMost(300f)
        }
        return 300f
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
        type.getDescriptionTranslation(member, mods)?.addToTooltip(tooltip)
    }
}