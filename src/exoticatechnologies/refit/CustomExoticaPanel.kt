package exoticatechnologies.refit

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.loading.specs.HullVariantSpec
import exoticatechnologies.ui.impl.shop.ShipModUIPlugin

class CustomExoticaPanel {
    companion object {
        //Overwrite for Background Panel Width
        fun getWidth() : Float {
            return 960f
        }

        //Overwrite for Background Panel Height
        fun getHeight() : Float {
            return 540f
        }

        fun renderDefaultBorder() = true
        fun renderDefaultBackground() = true
    }

    fun init(backgroundPanel: CustomPanelAPI, backgroundPlugin: ExoticaPanelPlugin, width: Float, height: Float, member: FleetMemberAPI, variant: HullVariantSpec) {

        /*
        var panel = backgroundPanel.createCustomPanel(width, height, null)
        backgroundPanel.addComponent(panel)

        var element = panel.createUIElement(width, height, false)
        element.addPara("Member: ${member.shipName}", 0f)
        panel.addUIElement(element)*/

        var plugin = ShipModUIPlugin(Global.getSector().campaignUI.currentInteractionDialog, backgroundPanel, width, height)
        plugin.layoutPanels()
        plugin.showPanel(member)

        //Call this whenever you need to close the panel.
        //backgroundPlugin.close()

        //Set to true whenever you make a change that needs to be reflected in the refit screen, like adding a hullmod or a stat change.
        //RefitButtonAdder.requiresVariantUpdate = true
    }



}