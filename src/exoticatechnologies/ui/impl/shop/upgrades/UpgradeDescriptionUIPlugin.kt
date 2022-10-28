package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.util.StringUtils

class UpgradeDescriptionUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
    var member: FleetMemberAPI,
    var mods: ShipModifications
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var descriptionTooltip: TooltipMakerAPI? = null
    private var displayDescription: Boolean = true

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        resetDescription()

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    fun resetDescription() {
        if (descriptionTooltip != null) {
            mainPanel!!.removeComponent(descriptionTooltip)
        }

        val tooltip = mainPanel!!.createUIElement(panelWidth, panelHeight, false)
        descriptionTooltip = tooltip

        tooltip.setParaOrbitronLarge()
        tooltip.addPara(upgrade.name, upgrade.color, 0f)
        tooltip.setParaFontDefault()

        val levelText = StringUtils.getTranslation("UpgradesDialog", "UpgradeLevel")
            .format("level", mods.getUpgrade(upgrade))
            .toStringNoFormats()
        descriptionTooltip!!.addPara(levelText, upgrade.color,3f)

        val buttonText: String
        if (displayDescription) {
            descriptionTooltip!!.addPara(upgrade.description, 3f)
            buttonText = StringUtils.getString("UpgradesDialog", "ModStatsButtonText")
        } else {
            upgrade.printStatInfoToTooltip(tooltip, member, mods)
            buttonText = StringUtils.getString("UpgradesDialog", "ModDescriptionButtonText")
        }

        val descButton = descriptionTooltip!!.addButton(
            buttonText, "switchDescriptionButton",
            panelWidth - 6f, 18f, 0f
        )
        descButton.position.inTL(3f, 0f).setYAlignOffset(-panelHeight + 24f)
        buttons[descButton] = DescriptionSwapHandler(descButton, this)

        mainPanel!!.addUIElement(tooltip).inTL(0f, 0f)
    }

    fun swapDescription() {
        displayDescription = !displayDescription
        resetDescription()
    }

    private class DescriptionSwapHandler(val button: ButtonAPI, val shopPlugin: UpgradeDescriptionUIPlugin) : ButtonHandler() {
        override fun checked() {
            shopPlugin.buttons.remove(button)
            shopPlugin.swapDescription()
        }
    }
}