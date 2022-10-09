package exoticatechnologies.ui.impl.shop

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import java.awt.Color
import kotlin.math.max

class ShipModUIPanelPlugin(val dialog: InteractionDialogAPI,
                           var parentPanel: CustomPanelAPI, override var panelWidth: Float, override var panelHeight: Float) : InteractiveUIPanelPlugin() {
    private val pad = 3f
    private val opad = 10f

    private var innerPanel: CustomPanelAPI? = null
    private var innerTooltip: TooltipMakerAPI? = null
    private var shipHeaderPanel: CustomPanelAPI? = null
    private var shopMenuPanel: CustomPanelAPI? = null
    private var shopMenuTooltip: TooltipMakerAPI? = null
    private var shopMenuPlugin: ShopMenuUIPlugin? = null

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

    override fun renderBelow(alphaMult: Float) {
        RenderUtils.pushUIRenderingStack()
        RenderUtils.renderBox(pos.x, pos.y, pos.width, pos.height, Color.BLUE, 0.1F)
        RenderUtils.popUIRenderingStack()
    }

    fun showPanel(member: FleetMemberAPI?): CustomPanelAPI? {
        if (shopMenuPlugin != null && member != null) {
            shopMenuPlugin!!.deactivated(this, member, ShipModFactory.getForFleetMember(member))
            shopMenuPlugin = null
            shopMenuTooltip = null
            shopMenuPanel = null
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

        val rowPlugin = ShipHeaderUIPanelPlugin(dialog, member, mods, innerPanel!!)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = max(panelHeight * 0.1f, Global.getSettings().screenHeight * 0.166f)
        shipHeaderPanel = rowPlugin.layoutPanel(tooltip)

        val tabHolderPlugin = ShopTabHolderUIPlugin()
        tabHolderPlugin.panelWidth = panelWidth
        tabHolderPlugin.panelHeight = 36f
        val tabHolderPanel = innerPanel!!.createCustomPanel(tabHolderPlugin.panelWidth, tabHolderPlugin.panelHeight, tabHolderPlugin)

        var lastButton: TooltipMakerAPI? = null
        ShopManager.shopMenuUIPlugins.forEach {
            val newButton = it.getTabUIPlugin().createTabButton(tabHolderPanel, member, mods)
            clickables[newButton] = it.getNewButtonHandler(this, member, mods)

            if (lastButton != null) {
                tabHolderPanel.addUIElement(newButton).rightOfMid(lastButton, pad)
            } else {
                tabHolderPanel.addUIElement(newButton).inTL(pad, pad)
            }
            lastButton = newButton
        }

        tooltip.addCustom(tabHolderPanel, opad)

        val shopMenuPlugin = ShopHolderUIPlugin()
        shopMenuPlugin.panelWidth = panelWidth
        shopMenuPlugin.panelHeight = panelHeight - rowPlugin.panelHeight - tabHolderPlugin.panelHeight - 36
        //for some reason need to subtract 36 from here when panel is much bigger.
        //this brings it above the *Confirm* button for exiting the panel.

        shopMenuPanel = innerPanel!!.createCustomPanel(shopMenuPlugin.panelWidth, shopMenuPlugin.panelHeight, shopMenuPlugin)
        tooltip.addCustom(shopMenuPanel, 0f).position.belowLeft(tabHolderPanel, pad)

        innerPanel!!.addUIElement(tooltip).inTL(0f, 0f)

        return tooltip
    }

    fun activatedTab(plugin: ShopMenuUIPlugin, member: FleetMemberAPI, mods: ShipModifications) {
        if (shopMenuTooltip != null) {
            shopMenuPanel!!.removeComponent(shopMenuTooltip)
            shopMenuTooltip = null
        }

        if (shopMenuPlugin == plugin) {
            shopMenuPlugin!!.deactivated(this, member, mods)
            shopMenuPlugin = null
            return
        }

        plugin.panelWidth = shopMenuPanel!!.position.width - pad * 2
        plugin.panelHeight = shopMenuPanel!!.position.height - pad * 2

        // if null then we didn't want to display anything for this member/mods
        plugin.layoutPanel(shopMenuPanel!!, member, mods)?.let {
            plugin.activated(this, member, mods)
            shopMenuPlugin = plugin
            shopMenuTooltip = it
            shopMenuPanel!!.addUIElement(it).inTL(pad, pad)
        }
    }

    class ShopHolderUIPlugin: BaseUIPanelPlugin() {
        override var bgColor: Color = Color(255, 255, 255, 40)
    }

    class ShopTabHolderUIPlugin: BaseUIPanelPlugin() {
        override var bgColor: Color = Color(255, 255, 0, 40)
    }
}