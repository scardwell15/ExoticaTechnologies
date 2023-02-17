package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.TimedUIPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.chips.UpgradeChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.ChipMethod
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.getMods
import java.awt.Color

class UpgradePanelUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
    var member: FleetMemberAPI,
    var market: MarketAPI
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var descriptionPlugin: UpgradeDescriptionUIPlugin? = null
    private var resourcesPlugin: UpgradeResourcesUIPlugin? = null
    private var methodsPlugin: UpgradeMethodsUIPlugin? = null
    private var chipsPlugin: UpgradeChipPanelUIPlugin? = null

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        descriptionPlugin = UpgradeDescriptionUIPlugin(mainPanel!!, upgrade, member)
        descriptionPlugin!!.panelWidth = panelWidth / 2f
        descriptionPlugin!!.panelHeight = panelHeight
        descriptionPlugin!!.layoutPanels().position.inTL(0f, 0f)

        resourcesPlugin = UpgradeResourcesUIPlugin(mainPanel!!, upgrade, member, market)
        resourcesPlugin!!.panelWidth = panelWidth / 2f - 6f
        resourcesPlugin!!.panelHeight = panelHeight / 10f * 7f - 6f
        resourcesPlugin!!.layoutPanels().position.inTR(3f, 3f)

        methodsPlugin = UpgradeMethodsUIPlugin(mainPanel!!, upgrade, member, market)
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
        val mods = member.getMods()

        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        method.apply(member, mods, upgrade, market)

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f)

        descriptionPlugin!!.resetDescription()
        resourcesPlugin!!.redisplayResourceCosts(method)
        methodsPlugin!!.createTooltip()
    }

    fun showChipsPanel() {
        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        chipsPlugin = UpgradeChipPanelUIPlugin(mainPanel!!, upgrade, member, market)
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

    fun clickedChipStack(stack: CargoStackAPI) {
        chipsPlugin!!.destroyTooltip()
        chipsPlugin = null

        val method = ChipMethod()
        method.upgradeChipStack = stack

        doUpgradeWithMethod(upgrade, method)
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

    private class ChipPanelListener(val mainPlugin: UpgradePanelUIPlugin): ChipPanelUIPlugin.Listener<UpgradeSpecialItemPlugin>() {
        override fun checkedBackButton() {
            mainPlugin.clickedChipPanelBackButton()
        }

        override fun checked(stack: CargoStackAPI, plugin: UpgradeSpecialItemPlugin) {
            mainPlugin.clickedChipStack(stack)
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