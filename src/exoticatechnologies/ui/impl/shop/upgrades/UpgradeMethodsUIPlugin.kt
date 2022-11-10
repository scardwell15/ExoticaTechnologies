package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.ui.impl.shop.upgrades.methods.ChipMethod
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.StringTooltip
import exoticatechnologies.util.StringUtils

class UpgradeMethodsUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
    var member: FleetMemberAPI,
    var mods: ShipModifications,
    var market: MarketAPI
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
        var nextButtonX = 0f
        var rowYOffset = 25f
        for (method in UpgradesHandler.UPGRADE_METHODS) {
            val buttonText = method.getOptionText(member, mods, upgrade, market)
            tooltip.setButtonFontDefault()

            val buttonWidth: Float = tooltip.computeStringWidth(buttonText) + 15f
            if (nextButtonX + buttonWidth >= panelWidth) {
                nextButtonX = 0f
                rowYOffset += 24f
                lastButton = null
            }

            if (upgrade.canUseUpgradeMethod(member, mods, method)) {
                var canUse = method.canUse(member, mods, upgrade, market)
                if (canUse) {
                    if (method.usesLevel()) {
                        val level = mods.getUpgrade(upgrade)
                        val maxLevel: Int = upgrade.getMaxLevel(member)
                        if (level + 1 > maxLevel) {
                            canUse = false
                        }
                    }
                }
                if (canUse) {
                    if (method.usesBandwidth()) {
                        val shipBandwidth = mods.getBandwidthWithExotics(member)
                        val usedBandwidth = mods.usedBandwidth
                        var upgradeUsage = upgrade.bandwidthUsage
                        if (method is ChipMethod) {
                            val stack = ChipMethod.getDesiredChip(member, mods, upgrade)
                            if (stack != null) {
                                val plugin = stack.plugin as UpgradeSpecialItemPlugin
                                upgradeUsage = upgrade.bandwidthUsage * (plugin.upgradeLevel - mods.getUpgrade(upgrade))
                            }
                        }
                        canUse = usedBandwidth + upgradeUsage <= shipBandwidth
                    }
                }

                val methodButton: ButtonAPI = tooltip.addButton(buttonText, "", buttonWidth, 18f, 2f)
                val tooltipText = method.getOptionTooltip(member, mods, upgrade, market)
                if (tooltipText != null) {
                    tooltip.addTooltipToPrevious(
                        StringTooltip(tooltip, tooltipText),
                        TooltipMakerAPI.TooltipLocation.BELOW
                    )
                }

                methodButton.isEnabled = canUse
                buttons[methodButton] = MethodButtonHandler(method, this)

                if (lastButton == null) {
                    methodButton.position.inTL(0f, rowYOffset.toFloat())
                } else {
                    methodButton.position.rightOfTop(lastButton, 3f)
                }
                lastButton = methodButton
                nextButtonX += buttonWidth + 3f
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

    open class MethodButtonHandler(val method: UpgradeMethod, val shopPlugin: UpgradeMethodsUIPlugin) : ButtonHandler() {
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