package exoticatechnologies.ui.impl.shop

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.tabs.TabPanelUIPlugin
import exoticatechnologies.ui.tabs.TabbedPanelUIPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import java.awt.Color
import kotlin.math.max

class ShipModUIPlugin(
    val dialog: InteractionDialogAPI?,
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
        private var lastTabOpen: String? = null
    }

    fun layoutPanels(): CustomPanelAPI {
        val outerPanel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        val outerTooltip = outerPanel.createUIElement(panelWidth, panelHeight, false)

        val headerStr = StringUtils.getTranslation("MainMenu", "ShipModMenu").toString()
        outerTooltip.addSectionHeading(headerStr, Alignment.MID, 0f)
        val heading = outerTooltip.prev

        innerPanel = outerPanel.createCustomPanel(panelWidth, panelHeight - 16f, null)

        innerTooltip = showNothing()

        outerTooltip.addCustom(innerPanel, 0f).position.belowMid(heading, 0f)
        outerPanel.addUIElement(outerTooltip).inTL(0f, 0f)
        parentPanel.addComponent(outerPanel)

        return outerPanel
    }

    fun showPanel(member: FleetMemberAPI?, variant: ShipVariantAPI? = member?.variant): CustomPanelAPI? {
        if (activeShopPlugin != null && member != null) {
            activeShopPlugin!!.deactivated(tabbedShopPlugin!!)
            activeShopPlugin = null
            tabbedShopPlugin = null
            shipHeaderPanel = null
        }

        innerPanel!!.removeComponent(innerTooltip)
        if (member != null) {
            innerTooltip = showMember(member, variant ?: member.variant)
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

    private fun showMember(member: FleetMemberAPI, variant: ShipVariantAPI): TooltipMakerAPI {
        val tooltip = innerPanel!!.createUIElement(panelWidth, panelHeight, false)
        val mods = ShipModLoader.get(member, variant) ?: ShipModFactory.generateForFleetMember(member)

        val rowPlugin = ShipHeaderUIPlugin(dialog, member, variant, mods, innerPanel!!)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = max(panelHeight * 0.1f, Global.getSettings().screenHeight * 0.16f)
        shipHeaderPanel = rowPlugin.layoutPanel(tooltip)
        shipHeaderPanel!!.position.inTL(1f, opad)

        tabbedShopPlugin = TabbedPanelUIPlugin(innerPanel!!)
        tabbedShopPlugin!!.panelWidth = panelWidth
        tabbedShopPlugin!!.panelHeight = panelHeight - rowPlugin.panelHeight - 36f

        val tabHolderPlugin = ShopTabHolderUIPlugin()
        tabHolderPlugin.panelWidth = panelWidth

        val shopPanel = tabbedShopPlugin!!.layoutPanels(
            tooltip,
            ShopManager.shopMenuUIPlugins,
            tabHolderPlugin,
            ShopHolderUIPlugin()
        )
        shopPanel.position.belowLeft(shipHeaderPanel, 0f)

        tabbedShopPlugin!!.addListener { plugin ->
            lastTabOpen = plugin.tabText
            activeShopPlugin = plugin
            tabHolderPlugin.lineColor = plugin.getTabButtonUIPlugin().baseColor
            if (plugin is ShopMenuUIPlugin) {
                plugin.member = member
                plugin.variant = variant
                plugin.mods = mods
                plugin.market = dialog?.interactionTarget?.market
            }
        }

        var openTab: ShopMenuUIPlugin? = null
        lastTabOpen?.let {
            var newTab: ShopMenuUIPlugin? = null
            for (plugin: ShopMenuUIPlugin in ShopManager.shopMenuUIPlugins) {
                if (plugin.tabText == it) {
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
            RenderUtils.renderBox(pos.x, pos.y - 3f, panelWidth, 1f, lineColor, 1f)

            RenderUtils.popUIRenderingStack()
        }
    }
}