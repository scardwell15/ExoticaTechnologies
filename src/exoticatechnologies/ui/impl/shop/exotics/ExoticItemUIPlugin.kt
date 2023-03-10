package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.ui.UIUtils
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getMods
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class ExoticItemUIPlugin(
    item: Exotic,
    var member: FleetMemberAPI,
    private val listPanel: ListUIPanelPlugin<Exotic>
) : ListItemUIPanelPlugin<Exotic>(item) {
    override var bgColor: Color = Color(200, 200, 200, 0)
    private val opad = 6f
    override var panelWidth: Float = 222f
    override var panelHeight: Float = 64f
    var wasHovered: Boolean = false

    var installed: Boolean = false
    var installedText: LabelAPI? = null
    var typeText: LabelAPI? = null

    var itemImage: TooltipMakerAPI? = null
    var itemInfo: TooltipMakerAPI? = null

    override fun advance(amount: Float) {
        val mods = member.getMods()
        if (mods.hasExotic(item) != installed) {
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

        val mods = member.getMods()
        installed = mods.hasExotic(item)
        val exoticData = mods.getExoticData(item) ?: ExoticData(item)

        itemImage = rowPanel.createUIElement(iconSize, panelHeight, false)
        exoticData.addExoticIcon(itemImage!!)
        rowPanel.addUIElement(itemImage).inLMid(0f)

        itemInfo = rowPanel.createUIElement(panelWidth - 9f - iconSize, 14f, false)
        itemInfo!!.addPara(item.name, 0f).position.inTL(0f, 0f)
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
            installedText = null
            typeText = null
        }

        UIUtils.autoResize(itemInfo!!)

        rowPanel.addUIElement(itemInfo).rightOfMid(itemImage,9f)
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