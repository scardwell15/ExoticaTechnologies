package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.refit.RefitButtonAdder
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.exotics.chips.ExoticChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.exotics.methods.ChipMethod
import exoticatechnologies.ui.impl.shop.exotics.methods.ExoticMethod

class ExoticPanelUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications,
    var market: MarketAPI?
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var descriptionPlugin: ExoticDescriptionUIPlugin? = null
    private var methodsPlugin: ExoticMethodsUIPlugin? = null
    private var resourcesPlugin: ExoticResourcesUIPlugin? = null
    private var chipsPlugin: ExoticChipPanelUIPlugin? = null
    private var chipsTooltip: TooltipMakerAPI? = null
    private var oldValue: Float = mods.getValue()

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        descriptionPlugin = ExoticDescriptionUIPlugin(panel, exotic, member, variant, mods)
        descriptionPlugin!!.panelWidth = panelWidth / 2
        descriptionPlugin!!.panelHeight = panelHeight
        descriptionPlugin!!.layoutPanels()

        resourcesPlugin = ExoticResourcesUIPlugin(panel, exotic, member, variant, mods, market)
        resourcesPlugin!!.panelWidth = panelWidth / 2
        resourcesPlugin!!.panelHeight = panelHeight / 2
        resourcesPlugin!!.layoutPanels()

        methodsPlugin = ExoticMethodsUIPlugin(panel, exotic, member, variant, mods, market)
        methodsPlugin!!.panelWidth = panelWidth / 2
        methodsPlugin!!.panelHeight = panelHeight / 2
        methodsPlugin!!.layoutPanels()
        methodsPlugin!!.addListener(MethodListener())

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    private var setChipDescription = false
    override fun advancePanel(amount: Float) {
        chipsPlugin?.let {
            if (it.highlightedItem != null) {
                setChipDescription = true
                descriptionPlugin!!.resetDescription(
                    it.highlightedItem!!.exoticData!!
                )
            }
        }

        if (setChipDescription && chipsPlugin == null) {
            descriptionPlugin!!.resetDescription()
            setChipDescription = false
        }

        val value = mods.getValue()
        if (value != oldValue) {
            oldValue = value
            if (chipsPlugin != null) {
                killChipsPanel()
                showChipsPanel()
            } else {
                methodsPlugin!!.destroyTooltip()
                methodsPlugin!!.layoutPanels()
            }
        }
    }

    fun checkedMethod(method: ExoticMethod): Boolean {
        if (method is ChipMethod) {
            //do something else.
            showChipsPanel()
            return true
        } else {
            applyMethod(exotic, method)
            return false
        }
    }

    fun highlightedMethod(method: ExoticMethod?): Boolean {
        resourcesPlugin!!.redisplayResourceCosts(method)
        return false
    }

    fun applyMethod(exotic: Exotic, method: ExoticMethod) {
        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        method.apply(member, variant, mods, exotic, market)

        if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT) {
            RefitButtonAdder.requiresVariantUpdate = true
        }

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f)

        descriptionPlugin!!.resetDescription()
        resourcesPlugin!!.redisplayResourceCosts(method)
        methodsPlugin!!.showTooltip()
    }

    fun showChipsPanel() {
        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        val pW = panelWidth / 2 - 6f
        val pH = panelHeight - 6f
        chipsTooltip = mainPanel!!.createUIElement(pW, pH, false)
        val innerPanel = mainPanel!!.createCustomPanel(pW, pH, null)

        chipsPlugin = ExoticChipPanelUIPlugin(innerPanel, exotic, member, variant, mods, market!!)
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

        resourcesPlugin!!.redisplayResourceCosts(null)
        methodsPlugin!!.showTooltip()
    }

    fun clickedChipStack(stack: CargoStackAPI) {
        chipsPlugin!!.destroyTooltip()
        chipsPlugin = null
        mainPanel!!.removeComponent(chipsTooltip)
        chipsTooltip = null

        val method = ChipMethod()
        method.chipStack = stack

        applyMethod(exotic, method)
    }

    private inner class MethodListener : ExoticMethodsUIPlugin.Listener() {
        override fun checked(method: ExoticMethod): Boolean {
            return this@ExoticPanelUIPlugin.checkedMethod(method)
        }

        override fun highlighted(method: ExoticMethod): Boolean {
            return this@ExoticPanelUIPlugin.highlightedMethod(method)
        }

        override fun unhighlighted(method: ExoticMethod): Boolean {
            return this@ExoticPanelUIPlugin.highlightedMethod(null)
        }
    }

    private inner class ChipPanelListener : ChipPanelUIPlugin.Listener<ExoticSpecialItemPlugin>() {
        override fun checkedBackButton() {
            this@ExoticPanelUIPlugin.killChipsPanel()
        }

        override fun checked(stack: CargoStackAPI, plugin: ExoticSpecialItemPlugin) {
            this@ExoticPanelUIPlugin.clickedChipStack(stack)
        }
    }
}