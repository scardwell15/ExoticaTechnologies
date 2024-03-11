package exoticatechnologies.ui2.impl.mods.exotics

import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.mods.ExoticaPanel
import exoticatechnologies.ui2.impl.mods.ModTabContext
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ExoticsPanel(context: ExoticaPanelContext) : ExoticaPanel(context) {
    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        val tabbedList = ExoticsListPanel(ExoticsListContext(context))
        tabbedList.renderBackground = true
        tabbedList.itemWidth = 256f
        tabbedList.panelWidth = innerWidth
        tabbedList.panelHeight = innerHeight
        tabbedList.layoutPanel(menuPanel, null)
    }
}

class ExoticsTabContext : ModTabContext(
    ExoticsPanel(ExoticaPanelContext()),
    "exotics",
    StringUtils.getString("Exotics", "Title"),
    TAB_COLOR
) {
    companion object {
        val TAB_COLOR = Color(100, 120, 160, 255)
    }
}