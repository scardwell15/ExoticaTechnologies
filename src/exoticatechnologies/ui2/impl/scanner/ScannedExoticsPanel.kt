package exoticatechnologies.ui2.impl.scanner

import com.fs.starfarer.api.ui.BaseTooltipCreator
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.impl.mods.exotics.ExoticsTabContext
import exoticatechnologies.util.StringUtils


class ScannedExoticsPanel(context: ScannedExoticsPanelContext) :
    RefreshablePanel<PanelContext>(context) {

    override fun refresh(menuPanel: CustomPanelAPI, context: PanelContext) {
        context as ScannedExoticsPanelContext
        val mods = context.mods

        var lastImg: TooltipMakerAPI? = null
        val iconPanel: CustomPanelAPI = menuPanel.createCustomPanel(panelWidth, panelHeight, null)
        for (exoticData in mods.getExoticSet()) {
            val exoIcon = iconPanel.createUIElement(64f, 64f, false)
            exoticData.addExoticIcon(exoIcon)
            exoIcon.addTooltipToPrevious(
                ExoticTooltip(exoIcon, exoticData),
                TooltipMakerAPI.TooltipLocation.BELOW
            )

            if (lastImg == null) {
                iconPanel.addUIElement(exoIcon).inTL(innerPadding, 0f)
            } else {
                iconPanel.addUIElement(exoIcon).rightOfTop(lastImg, innerPadding)
            }
            lastImg = exoIcon
        }

        menuPanel.addComponent(iconPanel)
    }
}


class ScannedExoticsPanelContext(
    val mods: ShipModifications
) : PanelContext

class ScannedExoticsTabContext(
    panel: RefreshablePanel<PanelContext>
) : ScannedModsTabContext(
    panel,
    "exotics",
    StringUtils.getString("Exotics", "Title"),
    ExoticsTabContext.TAB_COLOR
)

class ExoticTooltip(val tooltip: TooltipMakerAPI, val exoticData: ExoticData) : BaseTooltipCreator() {
    override fun getTooltipWidth(tooltipParam: Any): Float {
        return tooltip.computeStringWidth(exoticData.exotic.textDescription).coerceAtMost(300f)
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
        tooltip.addPara(exoticData.getNameTranslation().toStringNoFormats(), exoticData.getColor(), 0f)
        tooltip.addPara(exoticData.exotic.description, 0f)
    }
}
