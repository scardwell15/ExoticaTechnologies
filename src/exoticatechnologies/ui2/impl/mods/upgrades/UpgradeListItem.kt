package exoticatechnologies.ui2.impl.mods.upgrades

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui2.SpritePanelPlugin
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.tabs.TabListContext
import exoticatechnologies.ui2.tabs.TabListItem
import exoticatechnologies.ui2.tabs.TabListItemContext
import exoticatechnologies.util.StringUtils
import org.magiclib.kotlin.setBrightness
import java.awt.Color

class UpgradeListItem(upgrade: Upgrade, context: TabListItemContext<Upgrade, ExoticaPanelContext>) :
    TabListItem<Upgrade, ExoticaPanelContext>(upgrade, context) {

    private val member = panelContext.member
    private val variant = panelContext.variant
    private val fleet = panelContext.fleet
    private val mods = panelContext.mods

    private val upgradeSprite = Global.getSettings().getSprite("upgrades", item.icon)

    override fun decorate(menuPanel: CustomPanelAPI) {
        member ?: return
        variant ?: return
        mods ?: return

        val imagePanel = menuPanel.createCustomPanel(innerHeight, innerHeight, SpritePanelPlugin(upgradeSprite))
        menuPanel.addComponent(imagePanel).inLMid(innerPadding)

        val itemInfo = menuPanel.createUIElement(innerWidth - innerHeight - innerPadding * 2f, panelHeight, false)
        val nameLabel = itemInfo.addPara(item.name, 0f)
        val nameElement = itemInfo.prev

        if (mods.hasUpgrade(item)) {
            val levelText = itemInfo.addPara("", 0f)
            val upgradeLevel = mods.getUpgrade(item)
            StringUtils.getTranslation("Upgrades", "UpgradeLevel")
                .format("level", upgradeLevel, Misc.getHighlightColor())
                .setLabelText(levelText!!)

            nameLabel.position.inTL(0f, panelHeight / 2f - nameLabel.position.height)
            levelText.position.belowLeft(nameElement, 0f)
        } else {
            if (!item.canApplyImpl(member, variant, mods)) {
                nameLabel.setColor(Color(166, 166, 166))
                upgradeSprite.color = upgradeSprite.color.setBrightness(166)
            }

            nameLabel.position.inTL(0f, panelHeight / 2f - nameLabel.position.height)
        }

        menuPanel.addUIElement(itemInfo).rightOfMid(imagePanel,9f)
    }
}

class UpgradeItemContext(upgrade: Upgrade, context: ExoticaPanelContext) :
    TabListItemContext<Upgrade, ExoticaPanelContext>(upgrade, UpgradeItemPanel(upgrade, context)) {
    override val unselectedColor: Color
        get() = Misc.interpolateColor(item.color, Color.BLACK, 0.75f)
    override val highlightedColor: Color
        get() = Misc.interpolateColor(item.color, Color.BLACK, 0.6f)
    override val activeColor: Color
        get() = Misc.interpolateColor(item.color, Color.DARK_GRAY, 0.6f)
    override val activeHighlightedColor: Color
        get() = Misc.interpolateColor(item.color, Color.DARK_GRAY, 0.5f)

    override fun createListItem(listContext: TabListContext<Upgrade, ExoticaPanelContext>): TabListItem<Upgrade, ExoticaPanelContext> {
        return UpgradeListItem(item, this)
    }
}