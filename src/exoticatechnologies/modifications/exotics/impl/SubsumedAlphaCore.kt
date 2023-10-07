package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import java.awt.Color

class SubsumedAlphaCore(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color.cyan
    override var canDropFromCombat: Boolean = false

    override fun shouldShow(member: FleetMemberAPI, mods: ShipModifications, market: MarketAPI?): Boolean {
        return false
    }

    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI?): Boolean {
        return true
    }

    override fun canApply(member: FleetMemberAPI, mods: ShipModifications?): Boolean {
        if (member.fleetData == null
            || member.fleetData.fleet == null
        ) {
            return false
        }
        return if (member.fleetData.fleet.faction.id == Factions.OMEGA) {
            super.canApplyToVariant(member.variant)
        } else false
    }

    override fun countsTowardsExoticLimit(member: FleetMemberAPI): Boolean {
        return false
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI?): Boolean {
        return true
    }

    override fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        title: UIComponentAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData,
        expand: Boolean
    ) {
        if (expand) {
            StringUtils.getTranslation(key, "description")
                .addToTooltip(tooltip, title)
        }
    }

    /**
     * extra bandwidth added directly to ship.
     *
     * @param member
     * @param mods
     * @param exoticData
     * @return
     */
    override fun getExtraBandwidth(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData?): Float {
        return 50f * getPositiveMult(member, mods, exoticData)
    }
}