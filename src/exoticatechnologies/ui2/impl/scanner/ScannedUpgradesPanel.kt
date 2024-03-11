package exoticatechnologies.ui2.impl.scanner

import com.fs.starfarer.api.ui.BaseTooltipCreator
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.impl.mods.upgrades.UpgradesTabContext
import exoticatechnologies.util.RomanNumeral.toRoman
import exoticatechnologies.util.StringUtils


class ScannedUpgradesPanel(context: ScannedUpgradesPanelContext) :
    RefreshablePanel<PanelContext>(context) {

    override fun refresh(menuPanel: CustomPanelAPI, context: PanelContext) {
        context as ScannedUpgradesPanelContext
        val mods = context.mods

        var lastImg: TooltipMakerAPI? = null
        val iconPanel: CustomPanelAPI = menuPanel.createCustomPanel(panelWidth, panelHeight, null)
        for (upgrade in mods.getUpgradeMap().keys) {
            val upgIcon = iconPanel.createUIElement(64f, 64f, false)
            upgIcon.addImage(upgrade.iconPath, 64f, 0f)
            val imgComponent = upgIcon.prev
            upgIcon.addTooltipToPrevious(
                UpgradeTooltip(upgIcon, upgrade, mods),
                TooltipMakerAPI.TooltipLocation.BELOW
            )
            upgIcon.addPara(toRoman(mods.getUpgrade(upgrade)), 0f).position.rightOfTop(imgComponent, -32f)
            if (lastImg == null) {
                iconPanel.addUIElement(upgIcon).inTL(innerPadding, 0f)
            } else {
                iconPanel.addUIElement(upgIcon).rightOfTop(lastImg, innerPadding)
            }
            lastImg = upgIcon
        }

        menuPanel.addComponent(iconPanel)
    }
}

class ScannedUpgradesPanelContext(
    val mods: ShipModifications
) : PanelContext

class ScannedUpgradesTabContext(
    panel: RefreshablePanel<PanelContext>
) : ScannedModsTabContext(
    panel,
    "upgrades",
    StringUtils.getString("Upgrades", "Title"),
    UpgradesTabContext.TAB_COLOR
)

class UpgradeTooltip(
    private val tooltip: TooltipMakerAPI,
    private val upgrade: Upgrade,
    private val mods: ShipModifications
) : BaseTooltipCreator() {
    override fun getTooltipWidth(tooltipParam: Any): Float {
        return tooltip.computeStringWidth(upgrade.description).coerceAtMost(300f)
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
        StringUtils.getTranslation("FleetScanner", "UpgradeNameWithLevelAndMax")
            .format("upgradeName", upgrade.name, upgrade.color)
            .format("level", mods.getUpgrade(upgrade))
            .format("max", upgrade.maxLevel)
            .addToTooltip(tooltip)
        tooltip.addPara(upgrade.description, 0f)
    }
}