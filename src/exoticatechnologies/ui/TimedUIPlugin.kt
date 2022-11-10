package exoticatechnologies.ui

import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI

class TimedUIPlugin(private val endLife: Float, private val listener: Listener) : BaseUIPanelPlugin() {
    private var finished = false
    private var currLife = 0f

    override fun processInput(events: List<InputEventAPI>) {
        events.filter { pos.containsEvent(it) }
                .forEach { it.consume() }
    }

    override fun advance(amount: Float) {
        if (finished) return

        currLife += amount
        if (currLife >= endLife) {
            finished = true
            listener.end()
        }
    }

    override fun renderBelow(alphaMult: Float) {
        listener.renderBelow(pos, alphaMult, currLife, endLife)
    }

    override fun render(alphaMult: Float) {
        listener.render(pos, alphaMult, currLife, endLife)
    }

    interface Listener {
        fun end()
        fun render(pos: PositionAPI, alphaMult: Float, currLife: Float, endLife: Float)
        fun renderBelow(pos: PositionAPI, alphaMult: Float, currLife: Float, endLife: Float)
    }
}