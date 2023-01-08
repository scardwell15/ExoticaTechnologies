package exoticatechnologies.ui

import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import org.lwjgl.input.Mouse

open class InteractiveUIPanelPlugin : BaseUIPanelPlugin() {
    var highlightedButton: UIComponentAPI? = null
    val buttons : MutableMap<ButtonAPI, ButtonHandler> = hashMapOf()
    val clickables : MutableMap<UIComponentAPI, ButtonHandler> = hashMapOf()

    open fun advancePanel(amount: Float) {
    }

    final override fun advance(amount: Float) {
        var checkedButton: ButtonAPI? = null
        buttons.forEach { (button, _) ->
            if (button.isChecked && button.isEnabled) {
                checkedButton = button
                return@forEach
            }
        }

        checkedButton?.let {
            it.isChecked = false
            buttons[it]?.checked()
        }

        advancePanel(amount)
    }

    override fun processInput(events: List<InputEventAPI>) {

        if (highlightedButton != null) {
            checkHighlightedButton(highlightedButton!!, events)
        } else {
            checkButtons(events)
            checkClickables(events)
        }
    }

    fun checkClickables(events: List<InputEventAPI>) {
        clickables.forEach { (uiComp, listener) ->
            val containedEvents: List<InputEventAPI> = events.filter { uiComp.position.containsEvent(it) }

            if (containedEvents.isNotEmpty() && highlightedButton == null) {
                listener.highlighted()
                highlightedButton = uiComp
            }
        }
    }

    fun checkButtons(events: List<InputEventAPI>) {
        buttons.forEach { (button, listener) ->
            val containedEvents: List<InputEventAPI> = events.filter { button.position.containsEvent(it) }

            if (containedEvents.isNotEmpty()) {
                if (highlightedButton == null) {
                    listener.highlighted()
                    highlightedButton = button
                }
            }
        }
    }

    fun checkHighlightedButton(uiComp: UIComponentAPI, events: List<InputEventAPI>) {
        if (highlightedButton !is ButtonAPI) {
            val containedEvents: List<InputEventAPI> = events.filter { uiComp.position.containsEvent(it) }

            if (containedEvents.isEmpty()) {
                clickables[uiComp]?.unhighlighted()
                highlightedButton = null
            }

            containedEvents
                .takeIf { it.any { it.isLMBUpEvent } }
                ?.let {
                    clickables[uiComp]?.checked()
                }
        } else {
            val containedEvents: List<InputEventAPI> = events.filter { uiComp.position.containsEvent(it) }

            if (containedEvents.isEmpty() && !Mouse.isButtonDown(0)) {
                buttons[uiComp]?.unhighlighted()
                highlightedButton = null
            }
        }
    }
}