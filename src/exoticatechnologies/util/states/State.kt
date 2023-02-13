package exoticatechnologies.util.states

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.util.IntervalUtil

abstract class State {
    private var inited = false;
    val interval = IntervalUtil(getDuration(), getDuration())

    protected open fun init(ship: ShipAPI) {

    }

    fun advance(ship: ShipAPI, amount: Float) {
        if (!inited) {
            inited = true
            init(ship)
        }

        if (getDuration() > 0) {
            interval.advance(amount)
            if (interval.intervalElapsed() && intervalExpired(ship)) {
                return
            }
        }

        advanceShip(ship, amount)
    }

    /**
     * Return true to skip advanceShip
     */
    protected open fun intervalExpired(ship: ShipAPI): Boolean {
        return false;
    }

    protected abstract fun advanceShip(ship: ShipAPI, amount: Float)

    /**
     * If duration is <= 0, Interval will not advance.
     */
    protected abstract fun getDuration(): Float

    fun getProgressRatio(): Float {
        return interval.elapsed / interval.intervalDuration
    }
}