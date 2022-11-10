package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.StringTooltip
import exoticatechnologies.ui.impl.shop.ShopMenuUIPlugin
import exoticatechnologies.ui.tabs.TabButtonUIPlugin
import exoticatechnologies.ui.tabs.TabbedPanelUIPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ExoticShopUIPlugin: ShopMenuUIPlugin() {
    val pad: Float = 3f
    val opad: Float = 10f
    override var bgColor: Color = Color(200, 180, 40, 0)

    var innerPanel: CustomPanelAPI? = null
    var listPanel: CustomPanelAPI? = null
    var activeExotic: Exotic? = null
    var activePanel: CustomPanelAPI? = null

    override fun getNewTabButtonUIPlugin(): TabButtonUIPlugin {
        return ExoticTabUIPlugin()
    }

    override fun layoutPanel(holdingPanel: CustomPanelAPI, parentPlugin: TabbedPanelUIPlugin): TooltipMakerAPI? {
        val tooltip = holdingPanel.createUIElement(panelWidth, panelHeight, false)
        val panel = holdingPanel.createCustomPanel(panelWidth, panelHeight, this)


        val listPlugin = ExoticListUIPlugin(panel, member!!, mods!!)
        listPlugin.panelHeight = panelHeight - 22f
        listPanel = listPlugin.layoutPanels(ExoticsHandler.EXOTIC_LIST)

        listPlugin.addListener { member ->
            run {
                listPlugin.panelPluginMap.values.forEach { it.setBGColor(alpha = 0) }
                listPlugin.panelPluginMap[member]?.setBGColor(alpha = 100)!!
                showPanel(member)
            }
        }

        val panelPlugin = BaseUIPanelPlugin()
        panelPlugin.bgColor = Color(255, 0, 0, 0)
        innerPanel = panel.createCustomPanel(panelWidth - listPlugin.panelWidth, panelHeight, panelPlugin)
        panel.addComponent(innerPanel).rightOfTop(listPanel, pad)

        tooltip.addCustom(panel, 0f).position.inTL(pad, pad)
        holdingPanel.addUIElement(tooltip)

        return tooltip
    }

    fun showPanel(exotic: Exotic?) {
        if (activeExotic != null) {
            innerPanel!!.removeComponent(activePanel)
        }

        activeExotic = exotic
        if (activeExotic != null) {
            val upgradePlugin = ExoticPanelUIPlugin(innerPanel!!, exotic!!, member!!, mods!!, market!!)
            upgradePlugin.panelWidth = innerPanel!!.position.width
            upgradePlugin.panelHeight = innerPanel!!.position.height
            activePanel = upgradePlugin.layoutPanels()
            activePanel!!.position.inTL(0f, 0f)
        }
    }

    class ExoticTabUIPlugin: TabButtonUIPlugin(StringUtils.getString("ExoticsDialog", "OpenExoticOptions")) {
        override var panelWidth = 100f

        override val activeColor: Color = Color(255, 215, 100, 255)
        override val baseColor: Color = Color(100, 75, 30, 255)

        override fun createTabButton(holdingPanel: CustomPanelAPI, parentPlugin: TabbedPanelUIPlugin): TooltipMakerAPI {
            val tooltip = super.createTabButton(holdingPanel, parentPlugin)
            tooltip.addTooltipToPrevious(StringTooltip(tooltip, StringUtils.getString("ExoticsDialog","ExoticHelp")), TooltipMakerAPI.TooltipLocation.BELOW)
            return tooltip
        }
    }
}