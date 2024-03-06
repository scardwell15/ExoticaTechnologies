package exoticatechnologies.ui2.impl.scanner

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.tabs.PanelWithTabs
import exoticatechnologies.ui2.tabs.PanelWithTabsContext
import exoticatechnologies.ui2.tabs.TabContext
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ScannedMemberMods(context: ScannedMemberTabsContext) :
    PanelWithTabs<ScannedModsTabContext>(context) {
    override var bgColor: Color = Color(0, 0, 0, 255)

    override fun getTabPanelPositionPadding(): Vector2f {
        return Vector2f(0f, 0f)
    }

    override fun finishedRefresh(menuPanel: CustomPanelAPI, context: PanelWithTabsContext<ScannedModsTabContext>) {
        currContext.tabs.firstOrNull()?.let {
            pickedTab(it)
        }
    }
}

class ScannedMemberTabsContext(
    var member: FleetMemberAPI?,
    var mods: ShipModifications?
) : PanelWithTabsContext<ScannedModsTabContext>()

open class ScannedModsTabContext(
    panel: RefreshablePanel<ScannedModsTabContext>, tabId: String, tabText: String, tabColor: Color
) : TabContext<ScannedModsTabContext>(
    panel,
    tabId,
    tabText,
    tabColor
)