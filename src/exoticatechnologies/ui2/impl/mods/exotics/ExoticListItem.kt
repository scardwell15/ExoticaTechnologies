package exoticatechnologies.ui2.impl.mods.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.ui2.SpritePanelPlugin
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.tabs.TabListContext
import exoticatechnologies.ui2.tabs.TabListItem
import exoticatechnologies.ui2.tabs.TabListItemContext
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.magiclib.kotlin.setAlpha
import org.magiclib.kotlin.setBrightness
import java.awt.Color

class ExoticListItem(exotic: Exotic, context: TabListItemContext<Exotic, ExoticaPanelContext>) :
    TabListItem<Exotic, ExoticaPanelContext>(exotic, context) {

    private val member = panelContext.member
    private val variant = panelContext.variant
    private val fleet = panelContext.fleet
    private val mods = panelContext.mods

    private val upgradeSprite = Global.getSettings().getSprite("exotics", item.icon)

    override fun decorate(menuPanel: CustomPanelAPI) {
        member ?: return
        variant ?: return
        mods ?: return

        val imagePanel = menuPanel.createCustomPanel(innerHeight, innerHeight, SpritePanelPlugin(upgradeSprite))
        menuPanel.addComponent(imagePanel).inLMid(innerPadding)

        val itemInfo = menuPanel.createUIElement(innerWidth - innerHeight - innerPadding * 2f, panelHeight, false)
        val nameLabel = itemInfo.addPara(item.name, 0f)
        nameLabel.position.inTL(0f, panelHeight / 2f - nameLabel.position.height)

        val nameElement = itemInfo.prev
        val installed = mods.hasExotic(item)
        if (installed) {
            val installedText = StringUtils.getTranslation("ExoticsDialog", "Installed")
                .addToTooltip(itemInfo)
            installedText.position.belowLeft(nameElement, 0f)
            val installedComponent = itemInfo.prev

            val exoticData = mods.getExoticData(item) ?: ExoticData(item)
            if (exoticData.type != ExoticType.NORMAL) {
                itemInfo.setParaFontVictor14()
                val typeText = itemInfo.addPara(exoticData.type.name, exoticData.type.colorOverlay.setAlpha(255), 0f)
                typeText.position.belowLeft(installedComponent, 0f)
            }
        } else {
            nameLabel.setColor(Color(166, 166, 166))
            upgradeSprite.color = upgradeSprite.color.setBrightness(166)

            val quantity = Utilities.countChips(Global.getSector().playerFleet.cargo, item.key)

            if (quantity > 0) {
                val newText = StringUtils.getTranslation("CommonOptions", "InStockCount")
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

                    val installedText = itemInfo.addPara(newLabelText, 0f)
                    installedText.setHighlightColors(*typeColors.toTypedArray())
                    installedText.setHighlight(*typeLetters.toTypedArray())
                } else {
                    itemInfo.addPara(newText, Color(150, 150, 150), 0f)
                }

                itemInfo.prev.position.belowLeft(nameElement, 0f)
            }
        }

        menuPanel.addUIElement(itemInfo).rightOfMid(imagePanel,9f)
    }
}

class ExoticItemContext(exotic: Exotic, context: ExoticaPanelContext) :
    TabListItemContext<Exotic, ExoticaPanelContext>(exotic, ExoticItemPanel(exotic, context)) {
    override val unselectedColor: Color
        get() = Misc.interpolateColor(item.color, Color.BLACK, 0.75f)
    override val highlightedColor: Color
        get() = Misc.interpolateColor(item.color, Color.BLACK, 0.6f)
    override val activeColor: Color
        get() = Misc.interpolateColor(item.color, Color.DARK_GRAY, 0.6f)
    override val activeHighlightedColor: Color
        get() = Misc.interpolateColor(item.color, Color.DARK_GRAY, 0.5f)

    override fun createListItem(listContext: TabListContext<Exotic, ExoticaPanelContext>): TabListItem<Exotic, ExoticaPanelContext> {
        return ExoticListItem(item, this)
    }
}