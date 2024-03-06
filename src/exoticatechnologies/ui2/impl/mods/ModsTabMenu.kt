package exoticatechnologies.ui2.impl.mods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui2.*
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.tabs.PanelWithTabs
import exoticatechnologies.ui2.tabs.PanelWithTabsContext
import org.lwjgl.opengl.GL11
import org.magiclib.util.MagicUI
import java.awt.Color

class ModsTabMenu(context: ModMenuContext) :
    PanelWithTabs<ExoticaPanelContext>(context) {

    init {
        ModMenuManager.modMenuPanels.forEach { addTab(it) }

        addListener {
            (currContext.tabsContainerPlugin as ExoticaMenuTabContainer).lineColor = it.tabColor
        }
    }

    override fun finishedRefresh(menuPanel: CustomPanelAPI, context: PanelWithTabsContext<ExoticaPanelContext>) {
        currContext.tabs.firstOrNull()?.let {
            pickedTab(it)
        }
    }
}

class ModMenuContext(
    var member: FleetMemberAPI?,
    var variant: ShipVariantAPI?,
    var mods: ShipModifications?,
    var market: MarketAPI?
) : PanelWithTabsContext<ExoticaPanelContext>() {
    override var tabsContainerPlugin: RefreshablePanel<PanelContext> = ExoticaMenuTabContainer()
    constructor() : this(null, null, null, null)

    override fun getNewContext(panel: RefreshablePanel<ExoticaPanelContext>): ExoticaPanelContext {
        panel.currContext.copy(this)
        return panel.currContext
    }
}

class ExoticaMenuTabContainer : RefreshablePanel<PanelContext>(BasePanelContext()) {
    var lineColor: Color = Color(0, 0, 0, 0)
    override var bgColor: Color = Color(0, 0, 0, 0)

    override fun render(alphaMult: Float) {
        val x = pos.x
        val y = pos.y
        val width = pos.width
        val height = 2f

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

        GL11.glLineWidth(MagicUI.UI_SCALING * 2f)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_LOOP)

        GL11.glVertex2f(x, y)
        GL11.glColor4f(0f, 0f, 0f,
            lineColor.alpha / 255f * (alphaMult * 1f)
        )
        GL11.glVertex2f(x + width, y)

        GL11.glEnd()
        GL11.glPopMatrix()
    }
}