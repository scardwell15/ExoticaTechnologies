package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.modifications.exotics.types.ExoticTypeTooltip
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.util.StringUtils
import org.magiclib.kotlin.setAlpha

class ExoticDescriptionUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var descriptionTooltip: TooltipMakerAPI? = null

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        resetDescription()

        parentPanel.addComponent(panel).inTL(0f, 0f)

        return panel
    }

    fun resetDescription() {
        val exoticData = mods.getExoticData(exotic) ?: ExoticData(exotic)
        resetDescription(exoticData)
    }

    fun resetDescription(exoticData: ExoticData) {
        if (descriptionTooltip != null) {
            mainPanel!!.removeComponent(descriptionTooltip)
        }

        val tooltip = mainPanel!!.createUIElement(panelWidth, panelHeight, false)
        descriptionTooltip = tooltip

        tooltip.setParaOrbitronLarge()
        tooltip.addPara(exotic.name, exotic.color, 0f)
        if (exoticData.type != ExoticType.NORMAL) {
            tooltip.setParaFontVictor14()

            var typeText = exoticData.type.name
            if (!mods.hasExotic(exotic) || mods.getExoticData(exotic)!!.type != exoticData.type) {
                typeText = StringUtils.getTranslation("ExoticTypes", "NotInstalledText")
                    .format("typeName", typeText)
                    .toStringNoFormats()
            }
            tooltip.addPara(typeText, exoticData.type.colorOverlay.setAlpha(255), 0f)
            ExoticTypeTooltip.addToPrev(tooltip, member, mods, exoticData.type)
        }

        tooltip.setParaFontDefault()

        exotic.printDescriptionToTooltip(tooltip, member)
        tooltip.addPara("",3f)
        exotic.modifyToolTip(tooltip, tooltip.prev, member, mods, exoticData, true)

        mainPanel!!.addUIElement(tooltip).inTL(0f, 0f)
    }
}