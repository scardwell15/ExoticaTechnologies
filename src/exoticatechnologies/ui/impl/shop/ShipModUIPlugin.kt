package exoticatechnologies.ui.impl.shop

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.tabs.TabPanelUIPlugin
import exoticatechnologies.ui.tabs.TabbedPanelUIPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import java.awt.Color
import kotlin.math.max

class ShipModUIPlugin(
    val dialog: InteractionDialogAPI,
    var parentPanel: CustomPanelAPI, override var panelWidth: Float, override var panelHeight: Float
) : InteractiveUIPanelPlugin() {
    private val pad = 3f
    private val opad = 10f

    private var innerPanel: CustomPanelAPI? = null
    private var innerTooltip: TooltipMakerAPI? = null
    private var shipHeaderPanel: CustomPanelAPI? = null
    private var tabbedShopPlugin: TabbedPanelUIPlugin? = null
    private var activeShopPlugin: TabPanelUIPlugin? = null

    companion object {
        private var lastTabOpen: TabPanelUIPlugin? = null
    }

    fun layoutPanels(): CustomPanelAPI {
        val outerPanel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        val outerTooltip = outerPanel.createUIElement(panelWidth, panelHeight, false)

        val headerStr = StringUtils.getTranslation("MainMenu", "ShipModMenu").toString()
        outerTooltip.addSectionHeading(headerStr, Alignment.MID, 0f)
        val heading = outerTooltip.prev

        innerPanel = outerPanel.createCustomPanel(panelWidth, panelHeight - 16, null)

        innerTooltip = showNothing()

        outerTooltip.addCustom(innerPanel, 3f).position.belowMid(heading, 3f)
        outerPanel.addUIElement(outerTooltip).inTL(0f, 0f)
        parentPanel.addComponent(outerPanel).inTR(0f, 0f)

        return outerPanel
    }

    fun showPanel(member: FleetMemberAPI?): CustomPanelAPI? {
        if (activeShopPlugin != null && member != null) {
            activeShopPlugin!!.deactivated(tabbedShopPlugin!!)
            activeShopPlugin = null
            tabbedShopPlugin = null
            shipHeaderPanel = null
        }

        innerPanel!!.removeComponent(innerTooltip)
        if (member != null) {
            innerTooltip = showMember(member)
        } else {
            innerTooltip = showNothing()
        }
        return innerPanel
    }

    private fun showNothing(): TooltipMakerAPI? {
        val tooltip = innerPanel!!.createUIElement(panelWidth, panelHeight, false)

        innerPanel!!.addUIElement(tooltip)?.inTL(0f, 0f)

        return tooltip
    }

    private fun showMember(member: FleetMemberAPI): TooltipMakerAPI {
        val mods = ShipModFactory.getForFleetMember(member)

        val tooltip = innerPanel!!.createUIElement(panelWidth, panelHeight, false)

        val rowPlugin = ShipHeaderUIPlugin(dialog, member, mods, innerPanel!!)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = max(panelHeight * 0.1f, Global.getSettings().screenHeight * 0.16f)
        shipHeaderPanel = rowPlugin.layoutPanel(tooltip)

        tabbedShopPlugin = TabbedPanelUIPlugin(innerPanel!!)
        tabbedShopPlugin!!.panelWidth = panelWidth
        tabbedShopPlugin!!.panelHeight = panelHeight - rowPlugin.panelHeight - 36f

        val tabHolderPlugin = ShopTabHolderUIPlugin()
        val shopPanel = tabbedShopPlugin!!.layoutPanels(
            tooltip,
            ShopManager.shopMenuUIPlugins,
            tabHolderPlugin,
            ShopHolderUIPlugin()
        )
        shopPanel.position.belowLeft(shipHeaderPanel, opad)

        tabbedShopPlugin!!.addListener { plugin ->
            lastTabOpen = plugin
            activeShopPlugin = plugin
            tabHolderPlugin.lineColor = plugin.getTabButtonUIPlugin().baseColor
            if (plugin is ShopMenuUIPlugin) {
                plugin.member = member
                plugin.mods = ShipModFactory.getForFleetMember(member)
                plugin.market = dialog.interactionTarget.market
            }
        }

        var openTab = lastTabOpen
        openTab?.let {
            var newTab: ShopMenuUIPlugin? = null
            for (plugin: ShopMenuUIPlugin in ShopManager.shopMenuUIPlugins) {
                if (plugin::class == it::class) {
                    newTab = plugin
                }
            }
            openTab = newTab
        }

        tabbedShopPlugin!!.pickedTab(openTab ?: ShopManager.shopMenuUIPlugins[0])

        innerPanel!!.addUIElement(tooltip).inTL(0f, 0f)

        return tooltip
    }

    class ShopHolderUIPlugin : BaseUIPanelPlugin() {
        override var bgColor: Color = Color(255, 255, 255, 0)
    }

    class ShopTabHolderUIPlugin : BaseUIPanelPlugin() {
        var lineColor: Color = Color(0, 0, 0, 0)
        override var bgColor: Color = Color(255, 255, 0, 0)

        override fun renderBelow(alphaMult: Float) {
            RenderUtils.pushUIRenderingStack()

            //separator line
            RenderUtils.renderBox(pos.x, pos.y - 3f, pos.width, 1f, lineColor, 1f)

            RenderUtils.popUIRenderingStack()
        }
    }
}