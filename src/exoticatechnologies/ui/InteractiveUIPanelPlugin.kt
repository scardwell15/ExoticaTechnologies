package exoticatechnologies.ui

import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.UIComponentAPI

open class InteractiveUIPanelPlugin() : BaseUIPanelPlugin() {
    var highlightedButton: UIComponentAPI? = null
    val buttons : MutableMap<ButtonAPI, ButtonHandler> = hashMapOf()
    val clickables : MutableMap<UIComponentAPI, ButtonHandler> = hashMapOf()

    open fun advancePanel(amount: Float) {
    }

    override fun advance(amount: Float) {
        //check button APIs
        highlightedButton?.let {
            if (it is ButtonAPI && it.isHighlighted) {
                val listener: ButtonHandler = buttons[it]!!
                listener.unhighlighted()
                highlightedButton = null
            }
        }

        buttons.forEach { (button, listener) ->
            if (button.isChecked) {
                button.isChecked = false
                listener.checked()
            }

            if (button.isHighlighted && highlightedButton == null) {
                highlightedButton = button
                listener.highlighted()
            } else if (button == highlightedButton) {
                listener.unhighlighted()
                highlightedButton = null
            }
        }

        advancePanel(amount)
    }

    override fun processInput(events: List<InputEventAPI>) {
        //check clickables, which can be any UI component
        highlightedButton?.let { uiComp ->
            if (uiComp !is ButtonAPI) {
                val listener: ButtonHandler = clickables[uiComp]!!
                val containedEvents: List<InputEventAPI> = events.filter { uiComp.position.containsEvent(it) }

                if (containedEvents.isEmpty()) {
                    listener.unhighlighted()
                    highlightedButton = null
                }

                containedEvents
                    .takeIf { it.any { it.isLMBUpEvent } }
                    ?.let {
                        listener.checked()
                    }
            }
        }

        clickables.forEach { (uiComp, listener) ->
            val containedEvents: List<InputEventAPI> = events.filter { uiComp.position.containsEvent(it) }

            if (containedEvents.isNotEmpty() && highlightedButton == null) {
                listener.highlighted()
                highlightedButton = uiComp
            }
        }
    }
}