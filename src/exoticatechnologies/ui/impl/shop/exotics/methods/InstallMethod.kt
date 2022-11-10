package exoticatechnologies.ui.impl.shop.exotics.methods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

class InstallMethod : Method {
    override fun apply(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI): String {
        val stack = Utilities.getExoticChip(member.fleetData.fleet.cargo, exotic.key)
        if (stack != null) {
            Utilities.takeItem(stack)
        } else {
            exotic.removeItemsFromFleet(member.fleetData.fleet, member, market)
        }

        mods.putExotic(exotic)
        mods.save(member)
        ExoticaTechHM.addToFleetMember(member)
        exotic.onInstall(member)

        return StringUtils.getString("ExoticsDialog", "ExoticInstalled")
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI): Boolean {
        return !mods.hasExotic(exotic)
                && exotic.canApply(member)
                && (exotic.canAfford(member.fleetData.fleet, market) || Utilities.hasExoticChip(member.fleetData.fleet.cargo, exotic.key))
    }

    override fun canShow(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI): Boolean {
        return true
    }

    override fun getButtonText(exotic: Exotic): String {
        return StringUtils.getString("ExoticsDialog", "InstallExotic")
    }

    override fun getButtonTooltip(exotic: Exotic): String? {
        return null
    }

    override fun getResourceMap(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI,
        hovered: Boolean
    ): Map<String, Float>? {
        if (hovered) {
            val resourceCosts: MutableMap<String, Float> = exotic.getResourceCostMap(member, mods, market)

            if (exotic.getExtraBandwidth(member, mods) > 0) {
                resourceCosts[Bandwidth.BANDWIDTH_RESOURCE] = exotic.getExtraBandwidth(member, mods)
            }

            return resourceCosts
        }
        return null
    }
}