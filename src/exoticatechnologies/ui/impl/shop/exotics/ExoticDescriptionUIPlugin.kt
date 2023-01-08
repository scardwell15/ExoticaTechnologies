package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getMods

class ExoticDescriptionUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    var member: FleetMemberAPI
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var descriptionTooltip: TooltipMakerAPI? = null

    companion object {
        private var displayDescription: Boolean = true
    }

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        resetDescription()

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    fun resetDescription() {
        val mods = member.getMods()

        if (descriptionTooltip != null) {
            mainPanel!!.removeComponent(descriptionTooltip)
        }

        val tooltip = mainPanel!!.createUIElement(panelWidth, panelHeight, false)
        descriptionTooltip = tooltip

        tooltip.setParaOrbitronLarge()
        tooltip.addPara(exotic.name, exotic.color, 0f)
        tooltip.setParaFontDefault()

        val buttonText: String
        if (displayDescription) {
            exotic.printDescriptionToTooltip(tooltip, member)
            buttonText = StringUtils.getString("UpgradesDialog", "ModStatsButtonText")
        } else {
            exotic.modifyToolTip(tooltip, tooltip.prev, member, mods, true)
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

    private class DescriptionSwapHandler(val button: ButtonAPI, val shopPlugin: ExoticDescriptionUIPlugin) :
        ButtonHandler() {
        override fun checked() {
            shopPlugin.buttons.remove(button)
            shopPlugin.swapDescription()
        }
    }
}