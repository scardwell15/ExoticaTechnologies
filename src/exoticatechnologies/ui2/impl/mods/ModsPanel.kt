package exoticatechnologies.ui2.impl.mods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.ExoticaTabContext
import exoticatechnologies.ui2.impl.mods.bandwidth.BandwidthHeader
import java.awt.Color
import kotlin.math.max

class ModsPanel(context: ExoticaPanelContext) : ExoticaPanel(context) {
    override var outerPadding: Float = 4f

    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        val header = BandwidthHeader(context)
        header.panelWidth = innerWidth
        header.panelHeight = max(100f, Global.getSettings().screenHeight * 0.166f)
        val headerPanel = menuPanel.createCustomPanel(header.panelWidth, header.panelHeight, null)
        header.layoutPanel(headerPanel, context)
        menuPanel.addComponent(headerPanel).inTL(outerPadding, outerPadding)

        val modsTabMenu = ModsTabMenu(ModMenuContext(context.member, context.variant, context.mods, context.market))
        modsTabMenu.panelWidth = innerWidth
        modsTabMenu.panelHeight = innerHeight - header.panelHeight

        val modsTabPanel = modsTabMenu.layoutPanel(menuPanel, null)
        modsTabPanel.position.belowMid(headerPanel, outerPadding)
    }
}

class ModsPanelTabContext :
    ExoticaTabContext(ModsPanel(ExoticaPanelContext()), "mods", "Mods", Color(200, 110, 100, 255)) {
    override val highlightedColor: Color
        get() = Color(230, 140, 120, 255)
}