package exoticatechnologies.ui.impl.shop.exotics.methods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.ui.impl.shop.exotics.ExoticMethodsUIPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

class InstallMethod : ExoticMethod {
    override val key: String = "install"

    override fun apply(
        member: FleetMemberAPI,
        variant: ShipVariantAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI?
    ): String {
        val stack = Utilities.getSpecialStackWithData(Global.getSector().playerFleet.cargo, exotic.key)
        if (stack != null) {
            Utilities.takeItem(stack)
        } else {
            exotic.removeItemsFromFleet(Global.getSector().playerFleet, member, market)
        }

        mods.putExotic(ExoticData(exotic.key))

        ShipModLoader.set(member, variant, mods)
        ExoticaTechHM.addToFleetMember(member, variant)
        exotic.onInstall(member)

        return StringUtils.getString("ExoticsDialog", "ExoticInstalled")
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean {
        return !mods.hasExotic(exotic)
                && exotic.canApply(member, mods)
                && ExoticMethodsUIPlugin.isUnderExoticLimit(member, mods)
                && (exotic.canAfford(
            Global.getSector().playerFleet,
            market
        ) || Utilities.getSpecialStackWithData(Global.getSector().playerFleet.cargo, exotic.key) != null)
    }

    override fun canShow(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean {
        return !ChipMethod().canUse(
            member,
            mods,
            exotic,
            market
        ) || Utilities.getSpecialStackWithData(Global.getSector().playerFleet.cargo, exotic.key) != null
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
        market: MarketAPI?,
        hovered: Boolean
    ): Map<String, Float>? {
        if (hovered) {
            val resourceCosts: MutableMap<String, Float> = exotic.getResourceCostMap(member, mods, market)

            if (exotic.getExtraBandwidth(member, mods, mods.getExoticData(exotic)) > 0) {
                resourceCosts[Bandwidth.BANDWIDTH_RESOURCE] = exotic.getExtraBandwidth(
                    member,
                    mods,
                    mods.getExoticData(exotic)
                )
            }

            return resourceCosts
        }
        return null
    }
}