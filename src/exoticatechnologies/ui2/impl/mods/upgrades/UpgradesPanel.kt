package exoticatechnologies.ui2.impl.mods.upgrades

import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.mods.ExoticaPanel
import exoticatechnologies.ui2.impl.mods.ModTabContext
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradesPanel(context: ExoticaPanelContext) : ExoticaPanel(context) {
    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        val tabbedList = UpgradesListPanel(UpgradesListContext(context))
        tabbedList.renderBackground = true
        tabbedList.itemWidth = 256f
        tabbedList.panelWidth = innerWidth
        tabbedList.panelHeight = innerHeight
        tabbedList.layoutPanel(menuPanel, null)
    }
}

class UpgradesTabContext : ModTabContext(
    UpgradesPanel(ExoticaPanelContext()),
    "upgrades",
    StringUtils.getString("UpgradesDialog", "OpenUpgradeOptions"),
    TAB_COLOR
) {
    companion object {
        val TAB_COLOR = Color(160, 100, 100, 255)
    }
}