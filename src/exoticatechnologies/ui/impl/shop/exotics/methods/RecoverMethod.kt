package exoticatechnologies.ui.impl.shop.exotics.methods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import kotlin.math.max
import kotlin.math.min

class RecoverMethod : DestroyMethod() {
    override fun apply(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic): String {
        val fleet: CampaignFleetAPI = member.fleetData.fleet

        val stack = Utilities.getExoticChip(fleet.cargo, exotic.key)
        if (stack != null) {
            stack.add(1f)
        } else {
            fleet.cargo.addSpecial(exotic.newSpecialItemData, 1f)
        }

        Global.getSector().playerStats.storyPoints--

        /**
         * Super call
         */
        super.apply(member, mods, exotic)

        return StringUtils.getString("ExoticsDialog", "ExoticRecovered")
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI): Boolean {
        if (Global.getSector().playerStats.storyPoints >= 1f) {
            return super.canUse(member, mods, exotic, market)
        }
        return false
    }

    override fun getButtonText(exotic: Exotic): String {
        return StringUtils.getString("ExoticsDialog", "RecoverExotic")
    }

    override fun getResourceMap(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI,
        hovered: Boolean
    ): Map<String, Float>? {
        if (hovered) {
            val resourceCosts: MutableMap<String, Float> = mutableMapOf()
            resourceCosts[Utilities.STORY_POINTS] = 1f

            val stack = Utilities.getExoticChip(member.fleetData.fleet.cargo, exotic.key)
            if (stack != null) {
                resourceCosts.put(Utilities.formatSpecialItem(stack.specialDataIfSpecial), -1f)
            } else {
                resourceCosts.put(
                    "&" + StringUtils.getTranslation("ShipListDialog", "ChipName")
                        .format("name", exotic.name)
                        .toStringNoFormats(), -1f)
            }

            if (exotic.getExtraBandwidth(member, mods) > 0) {
                resourceCosts[Bandwidth.BANDWIDTH_RESOURCE] = -exotic.getExtraBandwidth(member, mods)
            }

            return resourceCosts
        }
        return null
    }
}