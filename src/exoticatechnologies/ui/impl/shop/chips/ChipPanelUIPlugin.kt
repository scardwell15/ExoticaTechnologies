package exoticatechnologies.ui.impl.shop.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ModSpecialItemPlugin
import exoticatechnologies.modifications.Modification
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.util.getMods

abstract class ChipPanelUIPlugin<T : ModSpecialItemPlugin>(
    var parentPanel: CustomPanelAPI,
    var mod: Modification,
    var member: FleetMemberAPI,
    var market: MarketAPI
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var mainTooltip: TooltipMakerAPI? = null
    private var listPlugin: ChipListUIPlugin? = null
    private val listeners: MutableList<Listener<T>> = mutableListOf()

    fun layoutPanels(): CustomPanelAPI {
        val panel: CustomPanelAPI = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel
        val tooltip: TooltipMakerAPI = panel.createUIElement(panelWidth, panelHeight, false)
        mainTooltip = tooltip
        val listPanel: CustomPanelAPI = panel.createCustomPanel(panelWidth, panelHeight, null)

        val mods = member.getMods()
        val upgradeChips: List<CargoStackAPI> = getChipSearcher().getChips(member.fleetData.fleet.cargo, member, mods, mod)

        listPlugin = getChipListPlugin(listPanel, member)
        listPlugin!!.panelWidth = panelWidth
        listPlugin!!.panelHeight = panelHeight - 28
        listPlugin!!.layoutPanels(upgradeChips).position.inTL(0f, 0f)

        listPlugin!!.addListener {
            this.clickedChipButton(it)
        }

        val backTooltip: TooltipMakerAPI = listPanel.createUIElement(panelWidth, 22f, false)
        val backButton: ButtonAPI = backTooltip.addButton(
            "Cancel", "backButton",
            Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.C2_MENU, 72f, 22f, 3f
        )

        backButton.position.inBMid(3f)
        buttons[backButton] = BackButtonHandler(this)

        listPanel.addUIElement(backTooltip).inBMid(0f)
        tooltip.addCustom(listPanel, 0f)
        panel.addUIElement(tooltip).inTL(0f, 0f)
        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    abstract fun getChipSearcher(): ChipSearcher<T>
    abstract fun getChipListPlugin(listPanel: CustomPanelAPI, member: FleetMemberAPI): ChipListUIPlugin

    fun destroyTooltip() {
        mainTooltip?.let {
            buttons.clear()
            listPlugin!!.clearItems()
            mainPanel?.removeComponent(it)
        }
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

    abstract class Listener<T: ModSpecialItemPlugin> {
        open fun checked(stack: CargoStackAPI, plugin: T) {
        }

        open fun checkedBackButton() {
        }
    }
}