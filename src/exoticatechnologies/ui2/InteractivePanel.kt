package exoticatechnologies.ui2

import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaUI.elements.LunaElement
import org.lwjgl.input.Mouse

open class InteractivePanel<T : PanelContext>(context: T) : BasePanel<T>(context) {
    var highlightedButton: UIComponentAPI? = null
    private val buttons: MutableMap<ButtonAPI, ComponentHandlers> = hashMapOf()
    private val clickables: MutableMap<UIComponentAPI, ComponentHandlers> = hashMapOf()
    var lunaElement: LunaElement? = null

    fun setupInteraction(menuPanel: CustomPanelAPI) {
        val tooltip = menuPanel.createUIElement(panelWidth, panelHeight, false)
        lunaElement = tooltip.addLunaElement(panelWidth, panelHeight).apply {
            renderBackground = false
            renderBorder = false
        }
        menuPanel.addUIElement(tooltip).inMid()
    }

    open fun advancePanel(amount: Float) {
    }

    fun clearEventHandlers() {
        buttons.clear()
        clickables.clear()
    }

    fun onClick(button: UIComponentAPI, handler: () -> Unit) {
        if (button is ButtonAPI) {
            val componentHandler = buttons[button] ?: ComponentHandlers()
            componentHandler.clicks.add(handler)
            buttons[button] = componentHandler
        } else {
            val componentHandler = clickables[button] ?: ComponentHandlers()
            componentHandler.clicks.add(handler)
            clickables[button] = componentHandler
        }
    }

    fun onMouseEnter(button: UIComponentAPI, handler: () -> Unit) {
        if (button is ButtonAPI) {
            val componentHandler = buttons[button] ?: ComponentHandlers()
            componentHandler.mouseEnters.add(handler)
            buttons[button] = componentHandler
        } else {
            val componentHandler = clickables[button] ?: ComponentHandlers()
            componentHandler.mouseEnters.add(handler)
            clickables[button] = componentHandler
        }
    }

    fun onMouseExit(button: UIComponentAPI, handler: () -> Unit) {
        if (button is ButtonAPI) {
            val componentHandler = buttons[button] ?: ComponentHandlers()
            componentHandler.mouseExits.add(handler)
            buttons[button] = componentHandler
        } else {
            val componentHandler = clickables[button] ?: ComponentHandlers()
            componentHandler.mouseExits.add(handler)
            clickables[button] = componentHandler
        }
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
            buttons[it]?.clicked()
        }

        advancePanel(amount)
    }

    override fun processInput(events: List<InputEventAPI>) {
        if (highlightedButton != null) {
            checkHighlightedButton(events)
        } else {
            checkButtons(events)
            checkClickables(events)
        }
    }

    private fun posContainsEvent(pos: PositionAPI, event: InputEventAPI): Boolean {
        val x = pos.x
        val y = pos.y
        val width = pos.width
        val height = pos.height
        return event.isMouseEvent && event.x.toFloat() in x..(x + width) && event.y.toFloat() in y..(y + height)
    }

    private fun checkClickables(events: List<InputEventAPI>) {
        clickables.forEach { (uiComp, listener) ->
            val compPos = uiComp.position
            val containedEvents: List<InputEventAPI> =
                events.filter { !it.isConsumed && posContainsEvent(compPos, it) }

            if (containedEvents.isNotEmpty() && highlightedButton == null) {
                containedEvents.forEach { it.consume() }
                listener.mouseEntered()
                highlightedButton = uiComp
            }
        }
    }

    private fun checkButtons(events: List<InputEventAPI>) {
        buttons.forEach { (button, listener) ->
            val compPos = button.position
            val containedEvents: List<InputEventAPI> =
                events.filter { !it.isConsumed && posContainsEvent(compPos, it) }

            if (containedEvents.isNotEmpty() && highlightedButton == null) {
                listener.mouseEntered()
                highlightedButton = button
            }
        }
    }

    private fun checkHighlightedButton(events: List<InputEventAPI>) {
        highlightedButton?.let { theButton ->
            val compPos = theButton.position
            if (theButton !is ButtonAPI) {
                val containedEvents: List<InputEventAPI> = events.filter { posContainsEvent(compPos, it) }

                if (containedEvents.isEmpty()) {
                    clickables[theButton]?.mouseExited()
                    highlightedButton = null
                }

                containedEvents
                    .filter { it.isLMBUpEvent }
                    .let { upEvents ->
                        if (upEvents.isNotEmpty()) {
                            clickables[highlightedButton]?.clicked()
                            upEvents.forEach { it.consume() }
                        }
                    }
            } else {
                val containedEvents: List<InputEventAPI> = events.filter { posContainsEvent(compPos, it) }

                if (containedEvents.isEmpty() && !Mouse.isButtonDown(0)) {
                    buttons[theButton]?.mouseExited()
                    highlightedButton = null
                }
            }
            return@let
        }
    }
}