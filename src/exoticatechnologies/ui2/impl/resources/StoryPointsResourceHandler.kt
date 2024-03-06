package exoticatechnologies.ui2.impl.resources

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

class StoryPointsResourceHandler: ResourceStringHandler {
    override fun isHandlerFor(resource: String): Boolean {
        return resource == Utilities.STORY_POINTS
    }

    override fun handle(
        tooltip: TooltipMakerAPI,
        resource: String,
        value: Float,
        mods: ShipModifications,
        market: MarketAPI?
    ) {
        if (value > 0) {
            StringUtils.getTranslation("CommonOptions", "StoryPointCost")
                .format("storyPoints", StringUtils.formatCost(value), Misc.getStoryOptionColor())
                .addToTooltip(tooltip)
        }
    }
}