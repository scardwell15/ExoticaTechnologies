package exoticatechnologies.ui2.impl.mods

import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.tabs.TabContext
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import java.awt.Color

abstract class ModTabContext(
    panel: RefreshablePanel<ExoticaPanelContext>, tabId: String, tabText: String, tabColor: Color
) : TabContext<ExoticaPanelContext>(
    panel,
    tabId,
    tabText,
    tabColor
)