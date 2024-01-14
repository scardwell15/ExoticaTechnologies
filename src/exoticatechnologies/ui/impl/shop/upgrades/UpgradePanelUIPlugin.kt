package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.refit.RefitButtonAdder
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.TimedUIPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.chips.UpgradeChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.ChipMethod
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod
import exoticatechnologies.util.RenderUtils
import java.awt.Color

class UpgradePanelUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications,
    var market: MarketAPI?
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var descriptionPlugin: UpgradeDescriptionUIPlugin? = null
    private var resourcesPlugin: UpgradeResourcesUIPlugin? = null
    private var methodsPlugin: UpgradeMethodsUIPlugin? = null
    private var chipsPlugin: UpgradeChipPanelUIPlugin? = null
    private var chipsTooltip: TooltipMakerAPI? = null
    private var oldValue: Float = mods.getValue()

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        descriptionPlugin = UpgradeDescriptionUIPlugin(mainPanel!!, upgrade, member, variant, mods)
        descriptionPlugin!!.panelWidth = panelWidth / 2f
        descriptionPlugin!!.panelHeight = panelHeight
        descriptionPlugin!!.layoutPanels().position.inTL(0f, 0f)

        resourcesPlugin = UpgradeResourcesUIPlugin(mainPanel!!, upgrade, member, variant, mods, market)
        resourcesPlugin!!.panelWidth = panelWidth / 2f - 6f
        resourcesPlugin!!.panelHeight = panelHeight / 10f * 7f - 6f
        resourcesPlugin!!.layoutPanels().position.inTR(3f, 3f)

        methodsPlugin = UpgradeMethodsUIPlugin(mainPanel!!, upgrade, member, variant, mods, market)
        methodsPlugin!!.panelWidth = panelWidth / 2f - 6f
        methodsPlugin!!.panelHeight = panelHeight / 10f * 3f - 6f
        methodsPlugin!!.layoutPanels().position.inBR(3f, 13f)
        methodsPlugin!!.addListener(MethodListener())

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    fun checkedMethod(method: UpgradeMethod): Boolean {
        if (method is ChipMethod) {
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

        method.apply(member, variant, mods, upgrade, market)

        if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT) {
            RefitButtonAdder.requiresVariantUpdate = true
        }

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f)

        descriptionPlugin!!.resetDescription()
        resourcesPlugin!!.redisplayResourceCosts(method)
        methodsPlugin!!.createTooltip()
    }

    override fun advancePanel(amount: Float) {
        val value = mods.getValue()
        if (value != oldValue) {
            oldValue = value
            if (chipsPlugin != null) {
                killChipsPanel()
                showChipsPanel()
            } else {
                methodsPlugin!!.destroyTooltip()
                methodsPlugin!!.createTooltip()
            }
        }
    }

    fun showChipsPanel() {
        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        val pW = panelWidth / 2 - 6f
        val pH = panelHeight - 6f
        chipsTooltip = mainPanel!!.createUIElement(pW, pH, false)
        val innerPanel = mainPanel!!.createCustomPanel(pW, pH, null)

        chipsPlugin = UpgradeChipPanelUIPlugin(innerPanel, upgrade, member, variant, mods, market!!)
        chipsPlugin!!.panelWidth = pW
        chipsPlugin!!.panelHeight = pH
        chipsPlugin!!.layoutPanels().position.inTR(0f, 0f)

        chipsPlugin!!.addListener(ChipPanelListener())

        chipsTooltip!!.addCustom(innerPanel, 0f).position.inTL(0f, 0f)
        mainPanel!!.addUIElement(chipsTooltip).inTR(9f, 3f)
    }

    fun killChipsPanel() {
        chipsPlugin!!.destroyTooltip()
        chipsPlugin = null
        mainPanel!!.removeComponent(chipsTooltip)
        chipsTooltip = null
    }

    fun clickedChipPanelBackButton() {
        killChipsPanel()

        resourcesPlugin!!.redisplayResourceCosts(null)
        methodsPlugin!!.createTooltip()
    }

    fun clickedChipStack(stack: CargoStackAPI) {
        killChipsPanel()

        val method = ChipMethod()
        method.upgradeChipStack = stack

        doUpgradeWithMethod(upgrade, method)
    }

    private inner class MethodListener: UpgradeMethodsUIPlugin.Listener() {
        override fun checked(method: UpgradeMethod): Boolean {
            return this@UpgradePanelUIPlugin.checkedMethod(method)
        }

        override fun highlighted(method: UpgradeMethod): Boolean {
            return this@UpgradePanelUIPlugin.highlightedMethod(method)
        }

        override fun unhighlighted(method: UpgradeMethod): Boolean {
            return this@UpgradePanelUIPlugin.highlightedMethod(null)
        }
    }

    private inner class ChipPanelListener(): ChipPanelUIPlugin.Listener<UpgradeSpecialItemPlugin>() {
        override fun checkedBackButton() {
            this@UpgradePanelUIPlugin.clickedChipPanelBackButton()
        }

        override fun checked(stack: CargoStackAPI, plugin: UpgradeSpecialItemPlugin) {
            this@UpgradePanelUIPlugin.clickedChipStack(stack)
        }
    }

    private inner class UpgradedUIListener(val tooltip: TooltipMakerAPI) : TimedUIPlugin.Listener {
        override fun end() {
            this@UpgradePanelUIPlugin.mainPanel!!.removeComponent(tooltip)
            this@UpgradePanelUIPlugin.resourcesPlugin!!.redisplayResourceCosts(null)
            this@UpgradePanelUIPlugin.methodsPlugin!!.createTooltip()
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