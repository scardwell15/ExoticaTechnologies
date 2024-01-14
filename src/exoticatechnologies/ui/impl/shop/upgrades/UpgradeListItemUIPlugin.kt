package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui.SpritePanelPlugin
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import java.awt.Color

class UpgradeListItemUIPlugin(
    item: Upgrade,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications,
    private val listPanel: ListUIPanelPlugin<Upgrade>
) : ListItemUIPanelPlugin<Upgrade>(item) {
    override var bgColor: Color = Color(200, 200, 200, 0)
    private val opad = 6f
    private val upgradeSprite = Global.getSettings().getSprite("upgrades", item.icon)
    val selected
        get() = bgColor.alpha >= 100

    override var panelWidth: Float = 222f
    override var panelHeight: Float = 64f
    var wasHovered: Boolean = false

    var upgradeLevel: Int = 0
    var levelText: LabelAPI? = null
    var lastValue = 0f

    override fun advance(amount: Float) {
        val newValue = mods.getValue()
        if (newValue != lastValue) {
            lastValue = newValue
            valueUpdated()
        }
    }

    override fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
        val rowPanel: CustomPanelAPI =
            listPanel.parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        val imagePanel = rowPanel.createCustomPanel(iconSize, panelHeight, SpritePanelPlugin(upgradeSprite))
        // Ship image with tooltip of the ship class
        rowPanel.addComponent(imagePanel).inLMid(0f)

        val itemInfo = rowPanel.createUIElement(panelWidth - 9f - iconSize, panelHeight, false)

        var textColor = RenderUtils.INSTALLED_COLOR
        if (mods.hasUpgrade(item)) {
            upgradeSprite.color = RenderUtils.INSTALLED_COLOR
        } else if (!item.canApply(member, mods)) {
            upgradeSprite.color = RenderUtils.CANT_INSTALL_COLOR
            textColor = RenderUtils.mergeColors(Color.white, upgradeSprite.color, 0.5f)
        } else {
            upgradeSprite.color = RenderUtils.CAN_APPLY_COLOR
            textColor = RenderUtils.mergeColors(Color.white, upgradeSprite.color, 0.5f)
        }

        itemInfo.addPara(item.name, textColor, 0f).position.inBL(0f, 0f)
        val nameLabel = itemInfo.prev

        levelText = itemInfo.addPara("", 0f)
        levelText!!.position.belowLeft(nameLabel, 0f)

        rowPanel.addUIElement(itemInfo).rightOfTop(imagePanel,9f)

        // done, add row to TooltipMakerAPI
        tooltip.addCustom(rowPanel, opad)

        lastValue = mods.getValue()
        valueUpdated()

        panel = rowPanel

        return panel!!
    }

    private fun valueUpdated() {
        if (mods.hasUpgrade(item) || selected) {
            upgradeSprite.color = RenderUtils.INSTALLED_COLOR
        } else if (!item.canApply(member, mods)) {
            upgradeSprite.color = RenderUtils.CANT_INSTALL_COLOR
        } else {
            upgradeSprite.color = RenderUtils.CAN_APPLY_COLOR
        }

        if (mods.getUpgrade(item) != upgradeLevel) {
            upgradeLevel = mods.getUpgrade(item)
            StringUtils.getTranslation("UpgradesDialog", "UpgradeLevel")
                .format("level", upgradeLevel, Misc.getHighlightColor())
                .setLabelText(levelText!!)
        }

        if (upgradeLevel == 0) {
            levelText!!.text = ""

            if (!item.canApply(member, mods)) {
                val newText = StringUtils.getString("Conditions", "CannotApplyTitle")
                levelText!!.text = newText
                levelText!!.setHighlightColor(Color(200,100,100))
                levelText!!.setHighlight(newText)
            } else {
                val quantity = Utilities.countChips(Global.getSector().playerFleet.cargo, item.key)

                if (quantity > 0) {
                    val newText = StringUtils.getTranslation("CommonOptions", "InStockCount")
                        .format("count", quantity)
                        .toStringNoFormats()
                    levelText!!.text = newText
                    levelText!!.setHighlightColor(Color(150, 150, 150))
                    levelText!!.setHighlight(newText)
                }
            }
        }
    }

    override fun processInput(events: List<InputEventAPI>) {
        if (selected) return

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