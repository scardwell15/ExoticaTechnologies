package exoticatechnologies.ui.impl.shop

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeShopUIPlugin: ShopMenuUIPlugin() {
    override var bgColor: Color = Color(150, 255, 200, 40)

    override fun getNewTabUIPlugin(): TabButtonUIPlugin {
        return UpgradeTabUIPlugin()
    }

    override fun layoutPanel(
        shopPanel: CustomPanelAPI,
        member: FleetMemberAPI,
        mods: ShipModifications
    ): TooltipMakerAPI? {
        val tooltip = shopPanel.createUIElement(panelWidth, panelHeight, false)
        val panel = shopPanel.createCustomPanel(panelWidth, panelHeight, this)

        val list = UpgradeListUIPanelPlugin(panel, member, mods)
        list.panelHeight = panelHeight - 22f
        list.layoutPanels(UpgradesHandler.UPGRADES_LIST)

        tooltip.addCustom(panel, 0f).position.inTL(0f, 0f)

        return tooltip
    }

    class UpgradeTabUIPlugin: TabButtonUIPlugin() {
        override var panelWidth = 100f

        override val activeColor: Color = Color(220, 220, 220, 255)
        override val clickedColor: Color = Color(180, 180, 180, 255)
        override val highlightedColor: Color = Color(165, 195, 220, 255)
        override val baseColor: Color = Color(120, 120, 130, 255)

        override val tabText: String
            get() = StringUtils.getString("UpgradesDialog", "OpenUpgradeOptions")
    }
}