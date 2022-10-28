package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.StringTooltip
import exoticatechnologies.ui.impl.shop.exotics.methods.Method
import exoticatechnologies.util.StringUtils

class ExoticMethodsUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    var member: FleetMemberAPI,
    var mods: ShipModifications,
    var market: MarketAPI,
    var methods: List<Method>
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var methodsTooltip: TooltipMakerAPI? = null
    private var listeners: MutableList<Listener> = mutableListOf()

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        createTooltip()

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    fun createTooltip() {
        val tooltip = mainPanel!!.createUIElement(panelWidth, panelHeight, false)
        methodsTooltip = tooltip

        tooltip.addTitle(StringUtils.getString("UpgradeMethods", "UpgradeMethodsTitle"))

        //this list automatically places buttons on new rows if the previous row had too many
        var lastButton: UIComponentAPI? = null
        var nextButtonX = 0
        var rowYOffset = 25
        for (method in methods) {
            val buttonText = method.getButtonText(exotic)
            tooltip.setButtonFontDefault()

            val buttonWidth: Float = tooltip.computeStringWidth(buttonText) + 10f
            if (nextButtonX + buttonWidth >= panelWidth) {
                nextButtonX = 0
                rowYOffset += 24
                lastButton = null
            }

            if (method.canShow(member, mods, exotic, market)) {
                val methodButton: ButtonAPI = tooltip.addButton(buttonText, "", buttonWidth, 18f, 2f)

                method.getButtonTooltip(exotic)?.let {
                    tooltip.addTooltipToPrevious(
                        StringTooltip(tooltip, it),
                        TooltipMakerAPI.TooltipLocation.BELOW
                    )
                }

                methodButton.isEnabled = method.canUse(member, mods, exotic, market)
                buttons[methodButton] = MethodButtonHandler(method, this)

                if (lastButton == null) {
                    methodButton.position.inTL(0f, rowYOffset.toFloat())
                } else {
                    methodButton.position.rightOfTop(lastButton, 3f)
                }
                lastButton = methodButton
                nextButtonX += 100
            }
        }

        mainPanel!!.addUIElement(tooltip).inTL(0f, 0f)
    }

    fun destroyTooltip() {
        methodsTooltip?.let {
            buttons.clear()
            mainPanel!!.removeComponent(it)
        }
        methodsTooltip = null
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun callListenerHighlighted(method: Method) {
        listeners.forEach {
            if (it.highlighted(method)) {
                return
            }
        }
    }

    fun callListenerUnhighlighted(method: Method) {
        listeners.forEach {
            if (it.unhighlighted(method)) {
                return
            }
        }
    }

    fun callListenerChecked(method: Method) {
        listeners.forEach {
            if (it.checked(method)) {
                return
            }
        }
    }

    open class MethodButtonHandler(val method: Method, val shopPlugin: ExoticMethodsUIPlugin) :
        ButtonHandler() {
        override fun checked() {
            shopPlugin.callListenerChecked(method)
        }

        override fun highlighted() {
            shopPlugin.callListenerHighlighted(method)
        }

        override fun unhighlighted() {
            shopPlugin.callListenerUnhighlighted(method)
        }
    }

    abstract class Listener {
        /**
         * Return true to skip other listeners.
         */
        open fun checked(method: Method): Boolean {
            return false
        }

        /**
         * Return true to skip other listeners.
         */
        open fun highlighted(method: Method): Boolean {
            return false
        }

        /**
         * Return true to skip other listeners.
         */
        open fun unhighlighted(method: Method): Boolean {
            return false
        }
    }
}