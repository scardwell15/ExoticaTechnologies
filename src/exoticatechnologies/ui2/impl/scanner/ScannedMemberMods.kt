package exoticatechnologies.ui2.impl.scanner

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.tabs.PanelWithTabs
import exoticatechnologies.ui2.tabs.PanelWithTabsContext
import exoticatechnologies.ui2.tabs.TabButton
import exoticatechnologies.ui2.tabs.TabContext
import exoticatechnologies.util.StringUtils
import org.lazywizard.lazylib.opengl.ColorUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ScannedMemberMods(context: ScannedMemberModsContext) :
    PanelWithTabs<PanelContext>(context) {
    override var bgColor: Color = Color(0, 0, 0, 255)

    override fun getTabPanelSizeOverride(): Vector2f? {
        return Vector2f(TabButton.getTabTextWidth(StringUtils.getString("Upgrades", "Title")) + innerPadding * 2f, tabHeight)
    }

    override fun getTabPanelPositionPadding(): Vector2f {
        return Vector2f(0f, 0f)
    }

    override fun getPanelHolderSizeOverride(): Vector2f {
        return Vector2f(panelWidth - 68f, panelHeight)
    }

    override fun positionPanelHolder(
        activeTabHolder: CustomPanelAPI,
        tabsContainer: CustomPanelAPI
    ) {
        activeTabHolder.position.inTL((getTabPanelSizeOverride()?.x ?: 64f), innerPadding)
    }

    override fun positionTabButton(lastButtonPanel: CustomPanelAPI?, buttonPanel: CustomPanelAPI) {
        if (lastButtonPanel != null) {
            buttonPanel.position.belowMid(lastButtonPanel, tabPadding)
        } else {
            buttonPanel.position.inTL(tabPadding, 0f)
        }
    }

    override fun finishedRefresh(menuPanel: CustomPanelAPI, context: PanelWithTabsContext<PanelContext>) {
        currContext.tabs.firstOrNull()?.let {
            pickedTab(it)
        }
    }

    override fun doubleTapDestroysPanel(): Boolean {
        return false
    }
}

class ScannedMemberModsContext(
    var member: FleetMemberAPI,
    var mods: ShipModifications
) : PanelWithTabsContext<PanelContext>() {
    init {
        if (mods.hasUpgrades()) {
            val context = ScannedUpgradesPanelContext(mods)
            tabs.add(ScannedUpgradesTabContext(ScannedUpgradesPanel(context)))
        }

        if (mods.hasExotics()) {
            val context = ScannedExoticsPanelContext(mods)
            tabs.add(ScannedExoticsTabContext(ScannedExoticsPanel(context)))
        }
    }
}

open class ScannerTabButton(context: TabContext<PanelContext>): TabButton<PanelContext>(context) {
    override fun render(alphaMult: Float) {
        val cutSize = 8f
        pos.apply {
            button?.let {
                var color = currContext.unselectedColor
                if (currContext.isActive()) {
                    if (it.isHighlighted) {
                        color = currContext.activeHighlightedColor
                    } else {
                        color = currContext.tabColor
                    }
                } else if (it.isHighlighted) {
                    color = currContext.highlightedColor
                }

                ColorUtils.glColor(color)
            }

            GL11.glPushMatrix()

            GL11.glTranslatef(0f, 0f, 0f)
            GL11.glRotatef(0f, 0f, 0f, 1f)

            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glBegin(GL11.GL_POLYGON)
            GL11.glVertex2f(x, y)
            GL11.glVertex2f(x, y + height)
            GL11.glVertex2f(x + width, y + height)
            GL11.glVertex2f(x + width, y)
            GL11.glEnd()
            GL11.glPopMatrix()

            drawableString.text = currContext.tabText
            drawableString.anchor = LazyFont.TextAnchor.CENTER
            drawableString.baseColor = Color.BLACK
            drawableString.draw(x + width / 2f + 1f, y + height / 2f - 1f)
            drawableString.baseColor = currContext.activeHighlightedColor.brighter()
            drawableString.draw(x + width / 2f, y + height / 2f)
        }
    }
}

open class ScannedModsTabContext(
    panel: RefreshablePanel<PanelContext>, tabId: String, tabText: String, tabColor: Color
) : TabContext<PanelContext>(
    panel,
    tabId,
    tabText,
    tabColor
) {
    override fun createNewTabButton(tabbedPanelContext: PanelWithTabsContext<PanelContext>): TabButton<PanelContext> {
        return ScannerTabButton(this)
    }
}