package exoticatechnologies.ui.impl.shop.chips

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ModSpecialItemPlugin
import exoticatechnologies.modifications.Modification
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.InteractiveUIPanelPlugin

abstract class ChipPanelUIPlugin<T : ModSpecialItemPlugin>(
    var parentPanel: CustomPanelAPI,
    var mod: Modification,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications,
    var market: MarketAPI
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var mainTooltip: TooltipMakerAPI? = null
    private var innerPanel: CustomPanelAPI? = null
    private var innerTooltip: TooltipMakerAPI? = null
    private var listPlugin: ChipListUIPlugin? = null

    var highlightedItem: T? = null

    private val listeners: MutableList<Listener<T>> = mutableListOf()

    fun layoutPanels(): CustomPanelAPI {
        val panel: CustomPanelAPI = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel
        mainTooltip = mainPanel!!.createUIElement(panelWidth, panelHeight, false)
        innerPanel = mainPanel!!.createCustomPanel(panelWidth, panelHeight, null)
        innerTooltip = innerPanel!!.createUIElement(panelWidth, panelHeight, false)
        val listPanel: CustomPanelAPI = innerPanel!!.createCustomPanel(panelWidth, panelHeight, null)

        val upgradeChips: List<CargoStackAPI> =
            getChipSearcher().getChips(Global.getSector().playerFleet.cargo, member, mods, mod)

        listPlugin = getChipListPlugin(listPanel)
        listPlugin!!.panelWidth = panelWidth
        listPlugin!!.panelHeight = panelHeight - 28
        listPlugin!!.layoutPanels(upgradeChips).position.inTL(0f, 0f)

        listPlugin!!.addListener {
            this.clickedChipButton(it)
        }

        val backTooltip: TooltipMakerAPI = innerPanel!!.createUIElement(panelWidth, 22f, false)
        val backButton: ButtonAPI = backTooltip.addButton(
            "Cancel", "backButton",
            Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.C2_MENU, 72f, 22f, 3f
        )

        backButton.position.inBMid(3f)
        buttons[backButton] = BackButtonHandler(this)

        innerPanel!!.addUIElement(backTooltip).inBMid(0f)
        innerPanel!!.addUIElement(innerTooltip).inTL(0f, 0f)
        mainTooltip!!.addCustom(innerPanel, 0f)
        mainPanel!!.addUIElement(mainTooltip).inTL(0f, 0f)
        parentPanel.addComponent(mainPanel!!)

        return mainPanel!!
    }

    override fun advancePanel(amount: Float) {
        this.highlightedItem = null
        listPlugin?.hoveredPlugin?.let {
            this.highlightedItem = it.item.plugin as T?
        }
    }

    abstract fun getChipSearcher(): ChipSearcher<T>
    abstract fun getChipListPlugin(listPanel: CustomPanelAPI): ChipListUIPlugin

    fun destroyTooltip() {
        mainTooltip?.let {
            buttons.clear()
            listPlugin!!.clearItems()
        }

        innerPanel?.removeComponent(innerPanel)
        mainPanel?.removeComponent(mainTooltip)
        parentPanel.removeComponent(mainPanel)

        mainTooltip = null
    }

    fun clickedChipButton(stack: CargoStackAPI) {
        listeners.forEach {
            it.checked(stack, stack.plugin as T)
        }
    }

    fun clickedBackButton() {
        listeners.forEach {
            it.checkedBackButton()
        }
    }

    open class BackButtonHandler(private val panelPlugin: ChipPanelUIPlugin<*>) : ButtonHandler() {
        override fun checked() {
            panelPlugin.clickedBackButton()
        }
    }

    fun addListener(listener: Listener<T>) {
        listeners.add(listener)
    }

    abstract class Listener<T : ModSpecialItemPlugin> {
        open fun checked(stack: CargoStackAPI, plugin: T) {
        }

        open fun checkedBackButton() {
        }
    }
}