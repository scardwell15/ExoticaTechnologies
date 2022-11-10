package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeListItemUIPlugin(
    item: Upgrade,
    var member: FleetMemberAPI,
    var mods: ShipModifications,
    private val listPanel: ListUIPanelPlugin<Upgrade>
) : ListItemUIPanelPlugin<Upgrade>(item) {
    override var bgColor: Color = Color(200, 200, 200, 0)
    private val opad = 6f
    override var panelWidth: Float = 222f
    override var panelHeight: Float = 64f
    var wasHovered: Boolean = false

    var upgradeLevel: Int = 0
    var levelText: LabelAPI? = null
    override fun advance(amount: Float) {
        if (mods.getUpgrade(item) != upgradeLevel) {
            upgradeLevel = mods.getUpgrade(item)
            StringUtils.getTranslation("UpgradesDialog", "UpgradeLevel")
                .format("level", upgradeLevel)
                .setLabelText(levelText)
        }
    }

    override fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
        val rowPanel: CustomPanelAPI =
            listPanel.parentPanel.createCustomPanel(panelWidth, panelHeight, this)

        val itemImage = rowPanel.createUIElement(iconSize, panelHeight, false)
        // Ship image with tooltip of the ship class
        itemImage.addImage(item.icon, iconSize, 0f)
        rowPanel.addUIElement(itemImage).inLMid(0f)

        val itemInfo = rowPanel.createUIElement(panelWidth - 9f - iconSize, panelHeight, false)

        itemInfo.addPara(item.name, 0f).position.inBL(0f, 0f)
        val nameLabel = itemInfo.prev

        levelText = itemInfo.addPara("", 0f)
        levelText!!.position.belowLeft(nameLabel, 0f)

        rowPanel.addUIElement(itemInfo).rightOfTop(itemImage,9f)

        // done, add row to TooltipMakerAPI
        tooltip.addCustom(rowPanel, opad)

        panel = rowPanel

        return panel!!
    }

    override fun processInput(events: List<InputEventAPI>) {
        if (bgColor.alpha >= 100) return // selected already

        if (isHovered(events)) {
            if (!wasHovered) {
                wasHovered = true
                setBGColor(alpha = 50)
            }
        } else if (wasHovered) {
            wasHovered = false
            setBGColor(alpha = 0)
        }
    }
}