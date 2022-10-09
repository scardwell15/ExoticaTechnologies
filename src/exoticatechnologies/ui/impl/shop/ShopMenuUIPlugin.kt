package exoticatechnologies.ui.impl.shop

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.util.RenderUtils
import java.awt.Color

abstract class ShopMenuUIPlugin: BaseUIPanelPlugin() {
    var tabPlugin: TabButtonUIPlugin? = null

    fun getTabUIPlugin(): TabButtonUIPlugin {
        if (tabPlugin == null) {
            tabPlugin = getNewTabUIPlugin()
        }
        return tabPlugin!!
    }

    protected abstract fun getNewTabUIPlugin(): TabButtonUIPlugin
    abstract fun layoutPanel(shopPanel: CustomPanelAPI, member: FleetMemberAPI, mods: ShipModifications): TooltipMakerAPI?

    fun getNewButtonHandler(plugin: ShipModUIPanelPlugin, member: FleetMemberAPI, mods: ShipModifications): TabButtonHandler {
        return TabButtonHandler(plugin, member, mods)
    }

    fun activated(plugin: ShipModUIPanelPlugin, member: FleetMemberAPI, mods: ShipModifications) {
        tabPlugin!!.activated(plugin, member, mods)
    }

    fun deactivated(plugin: ShipModUIPanelPlugin, member: FleetMemberAPI, mods: ShipModifications) {
        tabPlugin!!.deactivated(plugin, member, mods)
    }

    open inner class TabButtonHandler(val plugin: ShipModUIPanelPlugin, val member: FleetMemberAPI, val mods: ShipModifications): ButtonHandler() {
        override fun checked() {
            plugin.activatedTab(this@ShopMenuUIPlugin, member, mods)
        }
    }

    open class TabButtonUIPlugin: BaseUIPanelPlugin() {
        open val tabText: String
            get() = "TabText"
        override var panelWidth: Float = 96f
        override var panelHeight: Float = 32f

        open var active: Boolean = false
        open val activeColor: Color = Color(220, 220, 220, 255)
        open val clickedColor: Color = Color(180, 180, 180, 255)
        open val highlightedColor: Color = Color(220, 220, 220, 255)
        open val baseColor: Color = Color(120, 120, 130, 255)

        fun createTabButton(tabPanel: CustomPanelAPI, member: FleetMemberAPI, mods: ShipModifications): TooltipMakerAPI {
            val tooltip: TooltipMakerAPI = tabPanel.createUIElement(panelWidth, panelHeight, false)
            val panel: CustomPanelAPI = tabPanel.createCustomPanel(panelWidth, panelHeight, this)
            tooltip.addCustom(panel, 0f).position.inTL(0f, 0f)

            tooltip.setParaOrbitronLarge()
            tooltip.addPara(tabText, Color.white, 0f).position.inTMid(panelHeight / 2f - 10f)

            return tooltip
        }

        override fun processInput(events: List<InputEventAPI>) {
            if (active) {
                return
            }

            val relevents: List<InputEventAPI> = events
                        .filter { pos.containsEvent(it) }
            val highlighted = relevents.isNotEmpty()
            val clicked = relevents.any { it.isLMBUpEvent }

            bgColor = if (clicked) clickedColor
                        else if (highlighted) highlightedColor
                        else baseColor
        }

        fun activated(plugin: ShipModUIPanelPlugin, member: FleetMemberAPI, mods: ShipModifications) {
            active = true
            bgColor = activeColor
        }

        fun deactivated(plugin: ShipModUIPanelPlugin, member: FleetMemberAPI, mods: ShipModifications) {
            active = false
        }
    }
}