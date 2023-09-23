package exoticatechnologies.combat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import exoticatechnologies.combat.particles.ParticleController
import exoticatechnologies.modifications.Modification
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.upgrades.UpgradesHandler

class ExoticaEveryFramePlugin :
    BaseEveryFrameCombatPlugin() {

    override fun init(engine: CombatEngineAPI) {
        ParticleController.INSTANCE.PARTICLES.clear()
        engine.addLayeredRenderingPlugin(ParticleController.INSTANCE)
    }

    override fun advance(amount: Float, events: List<InputEventAPI>) {
        val engine = Global.getCombatEngine()
        val ships: List<ShipAPI> = engine.ships
        for (ship in ships) {
            val member = ship.fleetMember
            if (ship.fleetMember != null) {
                val mods = ShipModLoader.get(ship.fleetMember, ship.fleetMember.variant)
                if (mods != null) {
                    for (upgrade in UpgradesHandler.UPGRADES_LIST) {
                        if (!hasInitialized(upgrade, engine)) {
                            initialize(upgrade, engine)
                        }

                        if (mods.getUpgrade(upgrade) <= 0) continue
                        upgrade.advanceInCombatAlways(ship, member, mods)
                    }

                    for (exotic in ExoticsHandler.EXOTIC_LIST) {
                        if (!hasInitialized(exotic, engine)) {
                            initialize(exotic, engine)
                        }

                        if (!mods.hasExotic(exotic)) continue
                        exotic.advanceInCombatAlways(ship, member, mods, mods.getExoticData(exotic)!!)
                    }
                }
            }
        }
    }

    companion object {
        private fun hasInitialized(mod: Modification, engine: CombatEngineAPI = Global.getCombatEngine()): Boolean {
            return engine.customData.containsKey(mod.key)
        }

        private fun initialize(mod: Modification, engine: CombatEngineAPI = Global.getCombatEngine()) {
            engine.customData[mod.key] = true
            mod.init(engine)
        }
    }
}