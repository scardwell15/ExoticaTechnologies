package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeDescriptionUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
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

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    fun resetDescription() {
        if (descriptionTooltip != null) {
            mainPanel!!.removeComponent(descriptionTooltip)
        }

        val tooltip = mainPanel!!.createUIElement(panelWidth, panelHeight, false)
        descriptionTooltip = tooltip

        val color: Color = upgrade.color
        tooltip.setParaOrbitronLarge()
        tooltip.addPara(upgrade.name, color, 0f)
        tooltip.setParaFontDefault()

        val levelText = StringUtils.getTranslation("UpgradesDialog", "UpgradeLevel")
            .format("level", mods.getUpgrade(upgrade))
            .toStringNoFormats()
        descriptionTooltip!!.addPara(levelText, color,3f)

        upgrade.showDescriptionInShop(tooltip, member, mods)
        descriptionTooltip!!.addPara("", color,3f)
        upgrade.showStatsInShop(tooltip, member, mods)

        mainPanel!!.addUIElement(tooltip).inTL(0f, 0f)
    }
}