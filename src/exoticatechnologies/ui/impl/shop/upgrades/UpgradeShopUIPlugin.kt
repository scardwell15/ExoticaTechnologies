package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.StringTooltip
import exoticatechnologies.ui.impl.shop.ShopMenuUIPlugin
import exoticatechnologies.ui.tabs.TabButtonUIPlugin
import exoticatechnologies.ui.tabs.TabbedPanelUIPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeShopUIPlugin : ShopMenuUIPlugin() {
    val pad: Float = 3f
    val opad: Float = 10f
    override var bgColor: Color = Color(150, 255, 200, 0)
    override val tabText: String = "Upgrades"

    var innerPanel: CustomPanelAPI? = null
    var listPanel: CustomPanelAPI? = null
    var activeUpgrade: Upgrade? = null
    var activePanel: CustomPanelAPI? = null

    override fun getNewTabButtonUIPlugin(): TabButtonUIPlugin {
        return UpgradeTabUIPlugin()
    }

    override fun layoutPanel(holdingPanel: CustomPanelAPI, parentPlugin: TabbedPanelUIPlugin): TooltipMakerAPI? {
        val tooltip = holdingPanel.createUIElement(panelWidth, panelHeight, false)
        val panel = holdingPanel.createCustomPanel(panelWidth, panelHeight, this)


        val listPlugin = UpgradeListUIPlugin(panel, member!!, variant!!, mods!!, market)
        listPlugin.panelHeight = panelHeight - 22f
        listPanel = listPlugin.layoutPanels(UpgradesHandler.UPGRADES_LIST)

        listPlugin.addListener { member ->
            listPlugin.panelPluginMap.values.forEach { it.setBGColor(alpha = 0) }
            listPlugin.panelPluginMap[member]?.setBGColor(alpha = 100)!!
            showPanel(member)
        }

        val panelPlugin = BaseUIPanelPlugin()
        innerPanel = panel.createCustomPanel(panelWidth - listPlugin.panelWidth, panelHeight, panelPlugin)
        panel.addComponent(innerPanel).rightOfTop(listPanel, pad)

        tooltip.addCustom(panel, 0f).position.inTL(pad, pad)
        holdingPanel.addUIElement(tooltip)

        return tooltip
    }

    fun showPanel(upgrade: Upgrade?) {
        if (activeUpgrade != null) {
            innerPanel!!.removeComponent(activePanel)
        }

        activeUpgrade = upgrade
        if (activeUpgrade != null) {
            val upgradePlugin = UpgradePanelUIPlugin(innerPanel!!, upgrade!!, member!!, variant!!, mods!!, market)
            upgradePlugin.panelWidth = innerPanel!!.position.width
            upgradePlugin.panelHeight = innerPanel!!.position.height
            activePanel = upgradePlugin.layoutPanels()
            activePanel!!.position.inTL(0f, 0f)
        }
    }

    class UpgradeTabUIPlugin : TabButtonUIPlugin(StringUtils.getString("UpgradesDialog", "OpenUpgradeOptions")) {
        override var panelWidth = 100f

        override val activeColor: Color = Color(100, 180, 220, 255)
        override val baseColor: Color = Color(60, 80, 100, 255)

        override fun createTabButton(holdingPanel: CustomPanelAPI, parentPlugin: TabbedPanelUIPlugin): TooltipMakerAPI {
            val tooltip = super.createTabButton(holdingPanel, parentPlugin)
            tooltip.addTooltipToPrevious(
                StringTooltip(tooltip, StringUtils.getString("UpgradesDialog", "UpgradeHelp")),
                TooltipMakerAPI.TooltipLocation.BELOW
            )
            return tooltip
        }
    }
}