package exoticatechnologies.campaign

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetInflater
import com.fs.starfarer.api.campaign.listeners.FleetInflationListener
import exoticatechnologies.campaign.listeners.CampaignEventListener
    
class ExoticaFleetInflationListener : FleetInflationListener {
    override fun reportFleetInflated(fleet: CampaignFleetAPI?, inflater: FleetInflater?) {
        fleet?.let {
            CampaignEventListener.applyExtraSystemsToFleet(fleet)
        }
    }
}