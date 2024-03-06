package exoticatechnologies.ui2.impl

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui2.*
import exoticatechnologies.ui2.tabs.PanelWithTabs
import exoticatechnologies.ui2.tabs.PanelWithTabsContext
import exoticatechnologies.ui2.tabs.TabContext
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicUI
import java.awt.Color

class ExoticaMenu(context: ExoticaMenuContext) :
    PanelWithTabs<ExoticaPanelContext>(context) {
    var lineColor: Color = Misc.getDarkPlayerColor()
    override var bgColor: Color = Color(0, 0, 0, 255)

    override fun getTabPanelPositionPadding(): Vector2f {
        return Vector2f(0f, 0f)
    }

    init {
        ExoticaMenuManager.modMenuPanels.forEach { addTab(it) }

        addListener {
            lineColor = it.tabColor
        }
    }

    override fun render(alphaMult: Float) {
        val tabsPlugin = currContext.tabsContainerPlugin
        val x = pos.x
        val y = pos.y
        val width = pos.width
        val height = pos.height - tabsPlugin.pos.height - tabsPlugin.innerPadding

        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)

        GL11.glColor4f(
            lineColor.red / 255f,
            lineColor.green / 255f,
            lineColor.blue / 255f,
            lineColor.alpha / 255f * (alphaMult * 1f)
        )

        GL11.glLineWidth(MagicUI.UI_SCALING)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_LOOP)

        GL11.glVertex2f(x, y)
        GL11.glVertex2f(x, y + height)
        GL11.glVertex2f(x + width, y + height)
        GL11.glVertex2f(x + width, y)
        GL11.glVertex2f(x, y)

        GL11.glEnd()
        GL11.glPopMatrix()
    }

    override fun renderBelow(alphaMult: Float) {
        super.renderBelow(alphaMult)

        val tabsPlugin = currContext.tabsContainerPlugin
        val x = pos.x
        val y = pos.y
        val width = pos.width
        val height = pos.height - tabsPlugin.pos.height

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glColor4f(
            bgColor.red / 255f,
            bgColor.green / 255f,
            bgColor.blue / 255f,
            bgColor.alpha / 255f * (alphaMult * 1f)
        )
        GL11.glRectf(x, y, pos.x + width, y + height)

        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
    }

    override fun finishedRefresh(menuPanel: CustomPanelAPI, context: PanelWithTabsContext<ExoticaPanelContext>) {
        currContext.tabs.firstOrNull()?.let {
            pickedTab(it)
        }
    }
}

open class ExoticaTabContext(
    panel: RefreshablePanel<ExoticaPanelContext>, tabId: String, tabText: String, tabColor: Color
) : TabContext<ExoticaPanelContext>(
    panel,
    tabId,
    tabText,
    tabColor
)

class ExoticaMenuContext(
    var member: FleetMemberAPI?,
    var variant: ShipVariantAPI?,
    var mods: ShipModifications?,
    var market: MarketAPI?
) : PanelWithTabsContext<ExoticaPanelContext>() {
    constructor() : this(null, null, null, null)

    override fun getNewContext(panel: RefreshablePanel<ExoticaPanelContext>): ExoticaPanelContext {
        panel.currContext.copy(this)
        return panel.currContext
    }
}