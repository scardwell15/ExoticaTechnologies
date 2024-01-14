package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.ui.UIUtils
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class ExoticItemUIPlugin(
    item: Exotic,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications,
    private val listPanel: ListUIPanelPlugin<Exotic>
) : ListItemUIPanelPlugin<Exotic>(item) {
    override var bgColor: Color = Color(200, 200, 200, 0)
    private val opad = 6f
    private var exoticSprite: SpriteAPI? = null
    private var typeSprite: SpriteAPI? = null
    override var panelWidth: Float = 222f
    override var panelHeight: Float = 64f
    var wasHovered: Boolean = false
    val selected
        get() = bgColor.alpha >= 100

    var installed: Boolean = false
    var installedText: LabelAPI? = null
    var typeText: LabelAPI? = null

    var itemImage: TooltipMakerAPI? = null
    var itemInfo: TooltipMakerAPI? = null

    override fun advance(amount: Float) {
        if (mods.hasExotic(item) != installed) {
            if (mods.hasExotic(item) || selected) {
                exoticSprite?.color = RenderUtils.INSTALLED_COLOR
            } else if (!item.canApply(member, mods)) {
                exoticSprite?.color = RenderUtils.CANT_INSTALL_COLOR
                typeSprite?.color = RenderUtils.mergeColors(typeSprite?.color, exoticSprite?.color, 0.5f)
            } else {
                exoticSprite?.color = RenderUtils.CAN_APPLY_COLOR
                typeSprite?.color = RenderUtils.mergeColors(typeSprite?.color, exoticSprite?.color, 0.5f)
            }

            installed = mods.hasExotic(item)
            generate(panel!!)
        }
    }

    override fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
        val rowPanel: CustomPanelAPI =
            listPanel.parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        panel = rowPanel

        generate(rowPanel)

        // done, add row to TooltipMakerAPI
        tooltip.addCustom(rowPanel, 0f)


        return panel!!
    }

    fun generate(rowPanel: CustomPanelAPI) {
        itemImage?.let {
            panel!!.removeComponent(itemImage)
        }
        itemInfo?.let {
            panel!!.removeComponent(itemInfo)
        }

        installed = mods.hasExotic(item)
        val exoticData = mods.getExoticData(item) ?: ExoticData(item)

        itemImage = rowPanel.createUIElement(iconSize, panelHeight, false)

        var textColor = Color(255, 255, 255)
        val iconPlugins = exoticData.addExoticIcon(itemImage!!)
        exoticSprite = iconPlugins.first.sprite
        typeSprite = iconPlugins.second?.sprite
        if (mods.hasExotic(item)) {
            exoticSprite?.color = RenderUtils.INSTALLED_COLOR
        } else if (!item.canApply(member, mods)) {
            exoticSprite?.color = RenderUtils.CANT_INSTALL_COLOR
            typeSprite?.color = RenderUtils.mergeColors(typeSprite?.color, exoticSprite?.color, 0.5f)
            textColor = RenderUtils.mergeColors(Color.white, exoticSprite?.color, 0.5f)
        } else {
            exoticSprite?.color = RenderUtils.CAN_APPLY_COLOR
            typeSprite?.color = RenderUtils.mergeColors(typeSprite?.color, exoticSprite?.color, 0.5f)
            textColor = RenderUtils.mergeColors(Color.white, exoticSprite?.color, 0.5f)
        }

        rowPanel.addUIElement(itemImage).inLMid(0f)

        itemInfo = rowPanel.createUIElement(panelWidth - 9f - iconSize, 14f, false)
        itemInfo!!.addPara(item.name, textColor, 0f).position.inTL(0f, 0f)
        val nameLabel = itemInfo!!.prev

        if (installed) {
            installedText = StringUtils.getTranslation("ExoticsDialog", "Installed")
                .addToTooltip(itemInfo)
            installedText!!.position.belowLeft(nameLabel, 0f)
            val installedComponent = itemInfo!!.prev

            if (exoticData.type != ExoticType.NORMAL) {
                itemInfo!!.setParaFontVictor14()
                typeText = itemInfo!!.addPara(exoticData.type.name, exoticData.type.colorOverlay.setAlpha(255), 0f)
                typeText!!.position.belowLeft(installedComponent, 0f)
            } else {
                typeText = null
            }
        } else {
            val quantity = Utilities.countChips(Global.getSector().playerFleet.cargo, item.key)

            if (quantity > 0) {
                var newText = StringUtils.getTranslation("CommonOptions", "InStockCount")
                    .format("count", quantity)
                    .toStringNoFormats()

                val types = Utilities.getTypesInCargo(Global.getSector().playerFleet.cargo, item.key)
                    .filter { it != ExoticType.NORMAL }

                if (types.isNotEmpty()) {
                    val typeLetters = types.map { it.name.substring(0, 1) }.toMutableList()
                    val typeColors = types.map { it.colorOverlay.setAlpha(255) }.toMutableList()
                    typeColors.add(0, Color(150, 150, 150))

                    val newLabelText = newText + " | " + typeLetters.joinToString(separator = " ")

                    typeLetters.add(0, newText)

                    installedText = itemInfo!!.addPara(newLabelText, 0f)
                    installedText!!.setHighlightColors(*typeColors.toTypedArray())
                    installedText!!.setHighlight(*typeLetters.toTypedArray())
                } else {
                    installedText = itemInfo!!.addPara(newText, Color(150, 150, 150), 0f)
                }
            } else {
                installedText = null
            }
            typeText = null
        }

        UIUtils.autoResize(itemInfo!!)

        rowPanel.addUIElement(itemInfo).rightOfMid(itemImage, 9f)
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