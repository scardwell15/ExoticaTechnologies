package exoticatechnologies.ui.tabs

import com.fs.starfarer.api.ui.*
import exoticatechnologies.ui.BaseUIPanelPlugin
import java.awt.Color

open class TabButtonUIPlugin(var tabText: String): BaseUIPanelPlugin() {
    override var panelWidth: Float = 96f
    override var panelHeight: Float = 28f

    open var isButtonApi: Boolean = true
    open var button: ButtonAPI? = null
    open var active: Boolean = false
    open val activeColor: Color = Color(220, 220, 220, 255)
    open val baseColor: Color = Color(120, 120, 130, 255)

    open fun createTabButton(holdingPanel: CustomPanelAPI, parentPlugin: TabbedPanelUIPlugin): TooltipMakerAPI {
        val tooltip: TooltipMakerAPI = holdingPanel.createUIElement(panelWidth, panelHeight, false)
        button = tooltip.addButton(tabText, null, activeColor, baseColor, Alignment.MID, CutStyle.TOP, panelWidth, panelHeight, 0f)
        holdingPanel.addUIElement(tooltip)
        return tooltip
    }

    fun activated(plugin: TabbedPanelUIPlugin) {
        active = true
        bgColor = activeColor
    }

    fun deactivated(plugin: TabbedPanelUIPlugin) {
        active = false
        bgColor = baseColor
    }
}