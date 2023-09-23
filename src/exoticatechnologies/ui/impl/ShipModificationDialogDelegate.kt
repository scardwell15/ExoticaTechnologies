package exoticatechnologies.ui.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate.CustomDialogCallback
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemKeys
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.impl.ships.ShipListUIPlugin
import exoticatechnologies.ui.impl.shop.ShipModUIPlugin
import exoticatechnologies.util.MusicController
import exoticatechnologies.util.StringUtils
import kotlin.math.min

class ShipModificationDialogDelegate(var dialog: InteractionDialogAPI, var market: MarketAPI) : CustomDialogDelegate {
    var panelHeight: Float = (Global.getSettings().screenHeight * 0.65f).coerceAtLeast(540f)
    var panelWidth = min(Global.getSettings().screenWidth * 0.85f, 1280f)

    var members: MutableList<FleetMemberAPI> = mutableListOf()
    var plugin: CustomUIPanelPlugin = BaseUIPanelPlugin()

    override fun createCustomDialog(panel: CustomPanelAPI, callback: CustomDialogCallback) {
        MusicController.startMusic()

        members = Global.getSector().playerFleet.membersWithFightersCopy
            .filterNot { it.isFighterWing }
            .toMutableList()

        val listPlugin = ShipListUIPlugin(panel)
        listPlugin.panelHeight = panelHeight
        val shipListTooltip = listPlugin.layoutPanels(members)

        val modPlugin = ShipModUIPlugin(dialog, panel, panelWidth - listPlugin.panelWidth, panelHeight)
        val shipModTooltip = modPlugin.layoutPanels()
        shipModTooltip.position.inTR(0f, 0f)

        listPlugin.addListener { member ->
            run {
                listPlugin.panelPluginMap.values.forEach { it.setBGColor(alpha = 0) }
                listPlugin.panelPluginMap[member]?.setBGColor(alpha = 100)!!
                modPlugin.showPanel(member)
            }
        }
    }

    override fun hasCancelButton(): Boolean {
        return false
    }

    override fun getConfirmText(): String {
        return StringUtils.getString("Options", "leave")
    }

    override fun getCancelText(): String {
        return StringUtils.getString("Options", "cancel")
    }

    override fun customDialogConfirm() {
        MusicController.stopMusic()
        /*ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), dialog.plugin.memoryMap)
        dialog.plugin.memoryMap[MemKeys.LOCAL]!!["\$option", "ETDialogBack"] = 0f
        FireAll.fire(null, dialog, dialog.plugin.memoryMap, "DialogOptionSelected")*/
    }

    override fun customDialogCancel() {
        MusicController.stopMusic()
        //customDialogConfirm()
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin {
        return plugin
    }
}