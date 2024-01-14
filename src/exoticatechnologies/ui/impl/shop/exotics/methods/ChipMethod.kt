package exoticatechnologies.ui.impl.shop.exotics.methods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.exotics.ExoticMethodsUIPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

class ChipMethod : ExoticMethod {
    override val key: String = "chipInstall"

    var chipStack: CargoStackAPI? = null

    override fun apply(
        member: FleetMemberAPI,
        variant: ShipVariantAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI?
    ): String {
        val plugin = chipStack!!.plugin as ExoticSpecialItemPlugin
        val exoticData = plugin.exoticData!!
        mods.putExotic(exoticData)

        ShipModLoader.set(member, variant, mods)
        ExoticaTechHM.addToFleetMember(member, variant)
        exotic.onInstall(member)

        Utilities.takeItem(chipStack)

        return StringUtils.getString("ExoticsDialog", "ExoticInstalled")
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean {
        return !mods.hasExotic(exotic)
                && exotic.canApply(member, mods)
                && ExoticMethodsUIPlugin.isUnderExoticLimit(member, mods)
                && (Utilities.hasExoticChip(Global.getSector().playerFleet.cargo, exotic.key))
    }

    override fun canShow(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean {
        return canUse(member, mods, exotic, market)
    }

    override fun getButtonText(exotic: Exotic): String {
        return StringUtils.getString("ExoticsDialog", "InstallExoticChip")
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
            val resourceCosts: MutableMap<String, Float>
            val stacks: List<CargoStackAPI> = ExoticMethodsUIPlugin.getExoticChips(Global.getSector().playerFleet.cargo, member, mods, exotic)
            if (stacks.isNotEmpty()) {
                resourceCosts = mutableMapOf()
                resourceCosts[Utilities.formatSpecialItem(exotic.newSpecialItemData)] = 1f
            } else {
                resourceCosts = mutableMapOf("&" + StringUtils.getTranslation("ShipListDialog", "ChipName")
                    .format("name", exotic.name)
                    .toStringNoFormats()
                        to 1f)
            }

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