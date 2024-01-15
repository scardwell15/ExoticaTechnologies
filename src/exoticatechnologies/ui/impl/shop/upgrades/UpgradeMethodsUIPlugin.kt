package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.StringTooltip
import exoticatechnologies.ui.impl.shop.upgrades.methods.ChipMethod
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeMethodsUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications,
    var market: MarketAPI?
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var methodsTooltip: TooltipMakerAPI? = null
    private var listeners: MutableList<Listener> = mutableListOf()
    private var oldValue: Float = 0f

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        createTooltip()

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    fun createTooltip() {
        oldValue = mods.getValue()

        val tooltip = mainPanel!!.createUIElement(panelWidth, panelHeight, false)
        methodsTooltip = tooltip

        var prev: UIComponentAPI? = null
        if (mods.isMaxLevel(member, upgrade)) {
            tooltip.addTitle(StringUtils.getString("UpgradesDialog", "MaxLevelTitle"))
        } else if (!upgrade.canApply(member, mods)) {
            tooltip.addTitle(StringUtils.getString("Conditions", "CannotApplyTitle"), Color(200, 100, 100))
            showCannotApply(mods, tooltip)

            prev = tooltip.prev
        } else if (!checkBandwidth(mods)) {
            tooltip.addTitle(StringUtils.getString("Conditions", "CannotApplyTitle"), Color(200, 100, 100))
            StringUtils.getTranslation("Conditions", "CannotApplyBecauseBandwidth")
                .addToTooltip(tooltip)

            prev = tooltip.prev
        } else {
            tooltip.addTitle(StringUtils.getString("UpgradeMethods", "UpgradeMethodsTitle"))
        }
        showMethods(mods, tooltip, prev)

        mainPanel!!.addUIElement(tooltip).inTL(0f, 0f)
    }

    fun checkBandwidth(mods: ShipModifications): Boolean {
        val stack = ChipMethod.getDesiredChip(member, mods, upgrade)
        if (stack != null) {
            return true //usable chip
        } else {
            val shipBandwidth = mods.getBandwidthWithExotics(member)
            val usedBandwidth = mods.getUsedBandwidth()
            var upgradeUsage = upgrade.bandwidthUsage
            return usedBandwidth + upgradeUsage <= shipBandwidth
        }
    }

    fun showCannotApply(mods: ShipModifications, tooltip: TooltipMakerAPI) {
        val reasons: List<String> = upgrade.getCannotApplyReasons(member, mods)
        if (reasons.isNotEmpty()) {
            reasons.forEach {
                tooltip.addPara(it, 1f)
            }
        } else if (!upgrade.checkTags(member, mods, upgrade.tags)) {
            val names: List<String> = mods.getModsThatConflict(upgrade.tags).map { it.name }

            StringUtils.getTranslation("Conditions", "CannotApplyBecauseTags")
                .format("conflictMods", names.joinToString(", "))
                .addToTooltip(tooltip)
        }
    }

    fun showMethods(mods: ShipModifications, tooltip: TooltipMakerAPI, lastComponent: UIComponentAPI? = null) {
        //this list automatically places buttons on new rows if the previous row had too many
        var lastButton: UIComponentAPI? = null
        var nextButtonX = 0f
        var rowYOffset = 25f

        if (lastComponent != null) {
            rowYOffset += lastComponent.position.height
        }

        for (method in UpgradesHandler.UPGRADE_METHODS) {
            if (!method.canShow(member, mods, upgrade, market)) continue

            val buttonText = method.getOptionText(member, mods, upgrade, market)
            tooltip.setButtonFontDefault()

            val buttonWidth: Float = tooltip.computeStringWidth(buttonText) + 16f
            if (nextButtonX + buttonWidth >= panelWidth) {
                nextButtonX = 0f
                rowYOffset += 24f
                lastButton = null
            }

            if (upgrade.canUseUpgradeMethod(member, mods, method)) {
                val methodButton: ButtonAPI = tooltip.addButton(buttonText, "", buttonWidth, 18f, 2f)
                val tooltipText = method.getOptionTooltip(member, mods, upgrade, market)
                if (tooltipText != null) {
                    tooltip.addTooltipToPrevious(
                        StringTooltip(tooltip, tooltipText),
                        TooltipMakerAPI.TooltipLocation.BELOW
                    )
                }

                methodButton.isEnabled = (market != null || method.canUseIfMarketIsNull()) && method.canUse(member, mods, upgrade, market)
                buttons[methodButton] = MethodButtonHandler(method, this)

                if (lastButton == null) {
                    methodButton.position.inTL(0f, rowYOffset)
                } else {
                    methodButton.position.rightOfTop(lastButton, 3f)
                }
                lastButton = methodButton
                nextButtonX += buttonWidth + 3f
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

    fun callListenerHighlighted(method: UpgradeMethod) {
        listeners.forEach {
            if (it.highlighted(method)) {
                return
            }
        }
    }

    fun callListenerUnhighlighted(method: UpgradeMethod) {
        listeners.forEach {
            if (it.unhighlighted(method)) {
                return
            }
        }
    }

    fun callListenerChecked(method: UpgradeMethod) {
        listeners.forEach {
            if (it.checked(method)) {
                return
            }
        }
    }

    open class MethodButtonHandler(val method: UpgradeMethod, val shopPlugin: UpgradeMethodsUIPlugin) :
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
        open fun checked(method: UpgradeMethod): Boolean {
            return false
        }

        /**
         * Return true to skip other listeners.
         */
        open fun highlighted(method: UpgradeMethod): Boolean {
            return false
        }

        /**
         * Return true to skip other listeners.
         */
        open fun unhighlighted(method: UpgradeMethod): Boolean {
            return false
        }
    }
}