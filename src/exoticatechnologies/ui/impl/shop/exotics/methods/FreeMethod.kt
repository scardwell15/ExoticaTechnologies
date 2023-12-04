package exoticatechnologies.ui.impl.shop.exotics.methods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.ETModPlugin
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils

class FreeMethod : ExoticMethod {
    override val key: String = "free"

    override fun apply(
        member: FleetMemberAPI,
        variant: ShipVariantAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI?
    ): String {
        mods.putExotic(ExoticData(exotic.key))

        ShipModLoader.set(member, variant, mods)
        ExoticaTechHM.addToFleetMember(member, variant)
        exotic.onInstall(member)

        return StringUtils.getString("ExoticsDialog", "ExoticInstalled")
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean {
        return ETModPlugin.isDebugUpgradeCosts()
    }

    override fun canShow(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean {
        return canUse(member, mods, exotic, market)
    }

    override fun getButtonText(exotic: Exotic): String {
        return "Free"
    }

    override fun getButtonTooltip(exotic: Exotic): String? {
        return null
    }

    override fun getResourceMap(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI?,
        hovered: Boolean
    ): Map<String, Float>? {
        return null
    }
}