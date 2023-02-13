package exoticatechnologies.util.states

import com.fs.starfarer.api.combat.ShipAPI

abstract class StateWithNext(private val key: String): State() {
    protected abstract fun getNextState(): StateWithNext
    fun setNextState(ship: ShipAPI) {
        ship.setCustomData(key, getNextState())
    }
}