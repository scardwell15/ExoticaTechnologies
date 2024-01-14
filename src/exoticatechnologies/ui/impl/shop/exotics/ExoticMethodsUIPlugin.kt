package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.cargo.CrateItemPlugin
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.StringTooltip
import exoticatechnologies.ui.impl.shop.exotics.methods.ExoticMethod
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ExoticMethodsUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications,
    var market: MarketAPI?
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var methodsTooltip: TooltipMakerAPI? = null
    private var listeners: MutableList<Listener> = mutableListOf()

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        showTooltip()
        parentPanel.addComponent(panel).inBR(0f, 0f)

        return panel
    }

    fun showTooltip() {
        val tooltip = mainPanel!!.createUIElement(panelWidth, panelHeight, false)
        methodsTooltip = tooltip

        var prev: UIComponentAPI? = null
        if (mods.hasExotic(exotic)) {
            tooltip.addTitle(StringUtils.getString("ExoticsDialog", "InstalledTitle"))
        } else if (!exotic.canApply(member, mods)) {
            tooltip.addTitle(StringUtils.getString("Conditions", "CannotApplyTitle"), Color(200, 100, 100))
            showCannotApply(mods, tooltip)

            prev = tooltip.prev
        } else if (!isUnderExoticLimit(member, mods)) {
            tooltip.addTitle(StringUtils.getString("Conditions", "CannotApplyTitle"), Color(200, 100, 100))

            StringUtils.getTranslation("Conditions", "CannotApplyBecauseTooManyExotics")
                .addToTooltip(tooltip, Color(100, 200, 100))

            prev = tooltip.prev
        } else {
            tooltip.addTitle(StringUtils.getString("UpgradeMethods", "UpgradeMethodsTitle"))
        }

        showMethods(tooltip, mods, prev)

        mainPanel!!.addUIElement(tooltip).inTL(0f, 0f)
    }

    fun showCannotApply(mods: ShipModifications, tooltip: TooltipMakerAPI) {
        val reasons: List<String> = exotic.getCannotApplyReasons(member, mods)

        if (reasons.isNotEmpty()) {
            reasons.forEach {
                tooltip.addPara(it, 1f)
            }
        } else if (!exotic.checkTags(member, mods, exotic.tags)) {
            val names: List<String> = mods.getModsThatConflict(exotic.tags).map { it.name }

            StringUtils.getTranslation("Conditions", "CannotApplyBecauseTags")
                .format("conflictMods", names.joinToString(", "))
                .addToTooltip(tooltip)
        }
    }

    fun showMethods(tooltip: TooltipMakerAPI, mods: ShipModifications, lastComponent: UIComponentAPI? = tooltip.prev) {

        //this list automatically places buttons on new rows if the previous row had too many
        var lastButton: UIComponentAPI? = null
        var nextButtonX = 0f
        var rowYOffset = 25f

        if (lastComponent != null) {
            rowYOffset += lastComponent.position.height
        }

        for (method in ExoticsHandler.EXOTIC_METHODS) {
            val buttonText = method.getButtonText(exotic)
            tooltip.setButtonFontDefault()

            val buttonWidth: Float = tooltip.computeStringWidth(buttonText) + 16f
            if (nextButtonX + buttonWidth >= panelWidth) {
                nextButtonX = 0f
                rowYOffset += 24f
                lastButton = null
            }

            if (method.canShow(member, mods, exotic, market) && exotic.canUseMethod(member, mods, method)) {
                val methodButton: ButtonAPI = tooltip.addButton(buttonText, "", buttonWidth, 18f, 2f)

                method.getButtonTooltip(exotic)?.let {
                    tooltip.addTooltipToPrevious(
                        StringTooltip(tooltip, it),
                        TooltipMakerAPI.TooltipLocation.BELOW
                    )
                }

                methodButton.isEnabled =
                    (market != null || method.canUseIfMarketIsNull()) && method.canUse(member, mods, exotic, market)
                buttons[methodButton] = MethodButtonHandler(method, this)

                if (lastButton == null) {
                    methodButton.position.inTL(0f, rowYOffset)
                } else {
                    methodButton.position.rightOfTop(lastButton, 3f)
                }
                lastButton = methodButton
                nextButtonX += 100f
            }
        }
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

    fun callListenerHighlighted(method: ExoticMethod) {
        listeners.forEach {
            if (it.highlighted(method)) {
                return
            }
        }
    }

    fun callListenerUnhighlighted(method: ExoticMethod) {
        listeners.forEach {
            if (it.unhighlighted(method)) {
                return
            }
        }
    }

    fun callListenerChecked(method: ExoticMethod) {
        listeners.forEach {
            if (it.checked(method)) {
                return
            }
        }
    }

    open class MethodButtonHandler(val method: ExoticMethod, val shopPlugin: ExoticMethodsUIPlugin) :
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
        open fun checked(method: ExoticMethod): Boolean {
            return false
        }

        /**
         * Return true to skip other listeners.
         */
        open fun highlighted(method: ExoticMethod): Boolean {
            return false
        }

        /**
         * Return true to skip other listeners.
         */
        open fun unhighlighted(method: ExoticMethod): Boolean {
            return false
        }
    }

    companion object {
        @JvmStatic
        fun isUnderExoticLimit(member: FleetMemberAPI, mods: ShipModifications): Boolean {
            return mods.getMaxExotics(member) > mods.exotics.getCount(member)
        }

        fun getExoticChips(
            cargo: CargoAPI,
            member: FleetMemberAPI,
            mods: ShipModifications,
            exotic: Exotic
        ): List<CargoStackAPI> {
            val stacks: List<CargoStackAPI> = cargo.stacksCopy
                .flatMap { stack ->
                    if (stack.plugin is CrateItemPlugin)
                        getChipsFromCrate(stack, member, mods, exotic)
                    else
                        listOf(stack)
                }
                .filter { it.plugin is ExoticSpecialItemPlugin }
                .map { it to it.plugin as ExoticSpecialItemPlugin }
                .filter { (_, plugin) -> plugin.modId == exotic.key }
                .map { (stack, _) -> stack }

            return stacks
        }

        /**
         * gets all valid upgrade chips for member from crate
         */
        fun getChipsFromCrate(
            stack: CargoStackAPI,
            member: FleetMemberAPI,
            mods: ShipModifications,
            exotic: Exotic
        ): List<CargoStackAPI> {
            return getExoticChips((stack.plugin as CrateItemPlugin).cargo, member, mods, exotic)
        }
    }
}