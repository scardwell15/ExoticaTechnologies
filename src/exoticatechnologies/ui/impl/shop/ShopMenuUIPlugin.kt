package exoticatechnologies.ui.impl.shop

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.ui.tabs.TabPanelUIPlugin

abstract class ShopMenuUIPlugin : TabPanelUIPlugin() {
    var member: FleetMemberAPI? = null
    var market: MarketAPI? = null
}