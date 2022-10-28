package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.modifications.upgrades.methods.ChipMethod
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.TimedUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.chips.ChipPanelUIPlugin
import exoticatechnologies.util.RenderUtils
import java.awt.Color

class UpgradePanelUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
    var member: FleetMemberAPI,
    var mods: ShipModifications,
    var market: MarketAPI
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var descriptionPlugin: UpgradeDescriptionUIPlugin? = null
    private var resourcesPlugin: UpgradeResourcesUIPlugin? = null
    private var methodsPlugin: UpgradeMethodsUIPlugin? = null
    private var chipsPlugin: ChipPanelUIPlugin? = null

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        descriptionPlugin = UpgradeDescriptionUIPlugin(mainPanel!!, upgrade, member, mods)
        descriptionPlugin!!.panelWidth = panelWidth / 2f
        descriptionPlugin!!.panelHeight = panelHeight
        descriptionPlugin!!.layoutPanels().position.inTL(0f, 0f)

        resourcesPlugin = UpgradeResourcesUIPlugin(mainPanel!!, upgrade, member, mods, market)
        resourcesPlugin!!.panelWidth = panelWidth / 2f - 6f
        resourcesPlugin!!.panelHeight = panelHeight / 10f * 7f - 6f
        resourcesPlugin!!.layoutPanels().position.inTR(3f, 3f)

        methodsPlugin = UpgradeMethodsUIPlugin(mainPanel!!, upgrade, member, mods, market)
        methodsPlugin!!.panelWidth = panelWidth / 2f - 6f
        methodsPlugin!!.panelHeight = panelHeight / 10f * 3f - 6f
        methodsPlugin!!.layoutPanels().position.inBR(3f, 13f)
        methodsPlugin!!.addListener(MethodListener(this))

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    fun checkedMethod(method: UpgradeMethod): Boolean {
        if (method is ChipMethod) {
            //do something else.
            showChipsPanel()
            return true
        } else {
            doUpgradeWithMethod(upgrade, method)
            return false
        }
    }

    fun highlightedMethod(method: UpgradeMethod?): Boolean {
        resourcesPlugin!!.redisplayResourceCosts(method)
        return false
    }

    fun doUpgradeWithMethod(upgrade: Upgrade, method: UpgradeMethod) {
        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        val displayString = method.apply(member, mods, upgrade, market)

        val tooltip = mainPanel!!.createUIElement(panelWidth / 2, panelHeight, false)
        val timedPlugin = TimedUIPlugin(0.75f, UpgradedUIListener(this, tooltip))

        val upgradedPanel: CustomPanelAPI =
            mainPanel!!.createCustomPanel(panelWidth / 2, panelHeight, timedPlugin)
        val upgradedTooltip = upgradedPanel.createUIElement(panelWidth / 2, panelHeight, false)
        upgradedTooltip.addPara(displayString, 0f).position.inMid()

        upgradedPanel.addUIElement(upgradedTooltip).inTL(0f, 0f)
        tooltip.addCustom(upgradedPanel, 0f)
        mainPanel!!.addUIElement(tooltip).inBR(0f, 0f)
    }

    fun showChipsPanel() {
        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        chipsPlugin = ChipPanelUIPlugin(mainPanel!!, upgrade, member, mods, market)
        chipsPlugin!!.panelWidth = panelWidth / 2 - 6f
        chipsPlugin!!.panelHeight = panelHeight - 6f
        chipsPlugin!!.layoutPanels().position.inTR(9f, 3f)

        chipsPlugin!!.addListener(ChipPanelListener(this))
    }

    fun clickedChipPanelBackButton() {
        chipsPlugin!!.destroyTooltip()
        chipsPlugin = null

        resourcesPlugin!!.redisplayResourceCosts(null)
        methodsPlugin!!.createTooltip()
    }

    fun clickedChipStack(stack: CargoStackAPI, plugin: UpgradeSpecialItemPlugin) {
        chipsPlugin!!.destroyTooltip()
        chipsPlugin = null

        val method = ChipMethod()
        method.upgradeChipStack = stack
        val displayString = method.apply(member, mods, upgrade, market)

        val tooltip = mainPanel!!.createUIElement(panelWidth / 2, panelHeight, false)
        val timedPlugin = TimedUIPlugin(0.75f, UpgradedUIListener(this, tooltip))

        val upgradedPanel: CustomPanelAPI =
            mainPanel!!.createCustomPanel(panelWidth / 2, panelHeight, timedPlugin)
        val upgradedTooltip = upgradedPanel.createUIElement(panelWidth / 2, panelHeight, false)
        upgradedTooltip.addPara(displayString, 0f).position.inMid()

        upgradedPanel.addUIElement(upgradedTooltip).inTL(0f, 0f)
        tooltip.addCustom(upgradedPanel, 0f)
        mainPanel!!.addUIElement(tooltip).inBR(0f, 0f)
    }

    private class MethodListener(val mainPlugin: UpgradePanelUIPlugin): UpgradeMethodsUIPlugin.Listener() {
        override fun checked(method: UpgradeMethod): Boolean {
            return mainPlugin.checkedMethod(method)
        }

        override fun highlighted(method: UpgradeMethod): Boolean {
            return mainPlugin.highlightedMethod(method)
        }

        override fun unhighlighted(method: UpgradeMethod): Boolean {
            return mainPlugin.highlightedMethod(null)
        }
    }

    private class ChipPanelListener(val mainPlugin: UpgradePanelUIPlugin): ChipPanelUIPlugin.Listener() {
        override fun checkedBackButton() {
            mainPlugin.clickedChipPanelBackButton()
        }

        override fun checked(stack: CargoStackAPI, plugin: UpgradeSpecialItemPlugin) {
            mainPlugin.clickedChipStack(stack, plugin)
        }
    }

    private class UpgradedUIListener(val mainPlugin: UpgradePanelUIPlugin, val tooltip: TooltipMakerAPI) : TimedUIPlugin.Listener {
        override fun end() {
            mainPlugin.mainPanel!!.removeComponent(tooltip)
            mainPlugin.resourcesPlugin!!.redisplayResourceCosts(null)
            mainPlugin.methodsPlugin!!.createTooltip()
        }

        override fun render(pos: PositionAPI, alphaMult: Float, currLife: Float, endLife: Float) {

        }

        override fun renderBelow(pos: PositionAPI, alphaMult: Float, currLife: Float, endLife: Float) {
            RenderUtils.pushUIRenderingStack()
            val panelX = pos.x
            val panelY = pos.y
            val panelW = pos.width
            val panelH = pos.height
            RenderUtils.renderBox(
                panelX,
                panelY,
                panelW,
                panelH,
                Color.yellow,
                alphaMult * (endLife - currLife) / endLife
            )
            RenderUtils.popUIRenderingStack()
        }
    }
}