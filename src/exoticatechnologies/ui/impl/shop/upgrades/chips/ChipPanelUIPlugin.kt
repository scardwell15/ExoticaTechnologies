package exoticatechnologies.ui.impl.shop.upgrades.chips

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.cargo.CrateItemPlugin
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.ChipMethod

class ChipPanelUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
    var member: FleetMemberAPI,
    var mods: ShipModifications,
    var market: MarketAPI
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var mainTooltip: TooltipMakerAPI? = null
    private var listPlugin: ChipListUIPlugin? = null
    private val listeners: MutableList<Listener> = mutableListOf()

    fun layoutPanels(): CustomPanelAPI {
        val panel: CustomPanelAPI = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel
        val tooltip: TooltipMakerAPI = panel.createUIElement(panelWidth, panelHeight, false)
        mainTooltip = tooltip
        val listPanel: CustomPanelAPI = panel.createCustomPanel(panelWidth, panelHeight, null)

        val upgradeChips: List<CargoStackAPI> = getUpgradeChips(member.fleetData.fleet.cargo)

        listPlugin = ChipListUIPlugin(listPanel, member, mods)
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
            it.checked(stack, stack.plugin as UpgradeSpecialItemPlugin)
        }
    }

    fun clickedBackButton() {
        listeners.forEach {
            it.checkedBackButton()
        }
    }

    open class BackButtonHandler(private val panelPlugin: ChipPanelUIPlugin) : ButtonHandler() {
        override fun checked() {
            panelPlugin.clickedBackButton()
        }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    abstract class Listener {
        open fun checked(stack: CargoStackAPI, plugin: UpgradeSpecialItemPlugin) {
        }

        open fun checkedBackButton() {
        }
    }

    /**
     * gets all valid upgrade chips for member from cargo
     */
    fun getUpgradeChips(cargo: CargoAPI): List<CargoStackAPI> {
        return Companion.getUpgradeChips(cargo, member, mods, upgrade)
    }

    companion object {
        fun getUpgradeChips(cargo: CargoAPI, member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade): List<CargoStackAPI> {
            val stacks: List<CargoStackAPI> = cargo.stacksCopy
                .flatMap { stack ->
                    if (stack.plugin is CrateItemPlugin)
                        getChipsFromCrate(stack, member, mods, upgrade)
                    else
                        listOf(stack)
                }
                .filter { it.plugin is UpgradeSpecialItemPlugin }
                .map { it to it.plugin as UpgradeSpecialItemPlugin }
                .filter { (_, plugin) -> plugin.upgradeId == upgrade.key }
                .filter { (_, plugin) -> plugin.upgradeLevel > mods.getUpgrade(upgrade) }
                .filter { (_, plugin) -> mods.hasBandwidthForUpgrade(member, upgrade, plugin.upgradeLevel) }
                .filter { (stack, _) ->
                    ChipMethod.getCreditCost(member, mods, upgrade, stack) > Global.getSector().playerFleet.cargo.credits.get()
                }
                .map { (stack, _) -> stack }

            return stacks
        }

        /**
         * gets all valid upgrade chips for member from crate
         */
        fun getChipsFromCrate(stack: CargoStackAPI, member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade): List<CargoStackAPI> {
            return getUpgradeChips((stack.plugin as CrateItemPlugin).cargo, member, mods, upgrade)
        }
    }
}