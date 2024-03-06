package exoticatechnologies.ui2.impl

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.impl.mods.ModMenuContext
import exoticatechnologies.util.getFleetModuleSafe

open class ExoticaPanelContext(var member: FleetMemberAPI?, var variant: ShipVariantAPI?, var mods: ShipModifications?, var market: MarketAPI?): PanelContext {
    constructor(): this(null, null, null, null)

    val fleet: CampaignFleetAPI?
        get() = member?.getFleetModuleSafe()

    fun copy(context: ExoticaMenuContext) {
        member = context.member
        variant = context.variant
        mods = context.mods
        market = context.market
    }

    fun copy(context: ExoticaPanelContext) {
        member = context.member
        variant = context.variant
        mods = context.mods
        market = context.market
    }

    fun copy(context: ModMenuContext) {
        member = context.member
        variant = context.variant
        mods = context.mods
        market = context.market
    }

    fun duplicate(): ExoticaPanelContext {
        return ExoticaPanelContext(member, variant, mods, market)
    }
}