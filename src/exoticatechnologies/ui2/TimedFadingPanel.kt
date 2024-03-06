package exoticatechnologies.ui2

import com.fs.starfarer.api.input.InputEventAPI

open class TimedFadingPanel<T: PanelContext>(private val endLife: Float, currContext: T) :
    RefreshablePanel<T>(currContext) {

    private var finished = false
    private var currLife = 0f
    var beginFade = 0f
    var currAlpha = 1f
    var removeOnEnd = true

    override fun processInput(events: List<InputEventAPI>) {
        events.filter { pos.containsEvent(it) }
            .forEach { it.consume() }
    }

    override fun advancePanel(amount: Float) {
        if (finished) return

        currLife += amount

        if (beginFade <= currLife) {
            currAlpha = (endLife - currLife) / (endLife - beginFade)
        }

        if (currLife >= endLife) {
            finished = true

            if (removeOnEnd) {
                destroyPanel()
            }
        }
    }

    override fun renderBelow(alphaMult: Float) {
        super.renderBelow(alphaMult * currAlpha)
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult * currAlpha)
    }
}