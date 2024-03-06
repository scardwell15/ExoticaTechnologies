package exoticatechnologies.ui2.impl.mods

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.util.getFleetModuleSafe

open class ExoticaPanel(context: ExoticaPanelContext) : RefreshablePanel<ExoticaPanelContext>(context) {
    protected val member: FleetMemberAPI?
        get() = currContext.member
    protected val variant: ShipVariantAPI?
        get() = currContext.variant
    protected val mods: ShipModifications?
        get() = currContext.mods
    protected val fleet: CampaignFleetAPI?
        get() = currContext.member?.getFleetModuleSafe()
    protected val market: MarketAPI?
        get() = currContext.market
    protected val iconSize: Float
        get() = innerWidth.coerceAtMost(innerHeight)
}