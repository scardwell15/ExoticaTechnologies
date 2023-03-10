package exoticatechnologies.integration.exiledspace

import com.fs.starfarer.api.campaign.StarSystemAPI
import data.world.systems.ptes_baseSystemScript

class Exotica_UpgradesMapEffect : data.scripts.plugins.ptes_baseEffectPlugin {
    override fun beforeGeneration(system: StarSystemAPI?, genScript: ptes_baseSystemScript?) {
        //does nothing.
    }

    override fun afterGeneration(system: StarSystemAPI?, genScript: ptes_baseSystemScript?) {
        system?.fleets?.forEach {
            it.memoryWithoutUpdate["\$exotica_bandwidthMult"] = 2f
            it.memoryWithoutUpdate["\$exotica_upgradeMult"] = 2f
        }
    }
}