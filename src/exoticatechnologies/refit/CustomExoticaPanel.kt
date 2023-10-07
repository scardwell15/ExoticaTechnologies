package exoticatechnologies.refit

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.loading.specs.HullVariantSpec
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.ui.impl.shop.ShipModUIPlugin
import exoticatechnologies.util.MusicController
import exoticatechnologies.util.RenderUtils

class CustomExoticaPanel {
    companion object {
        //Overwrite for Background Panel Width
        fun getWidth() : Float {
            return (Global.getSettings().screenWidth * 0.66f)
        }

        //Overwrite for Background Panel Height
        fun getHeight() : Float {
            return (Global.getSettings().screenHeight * 0.65f)
        }

        fun renderDefaultBorder() = true
        fun renderDefaultBackground() = true
    }

    var x = 0f
    var y = 0f

    fun init(backgroundPanel: CustomPanelAPI, backgroundPlugin: ExoticaPanelPlugin, width: Float, height: Float, member: FleetMemberAPI, variant: HullVariantSpec) {

        /*
        var panel = backgroundPanel.createCustomPanel(width, height, null)
        backgroundPanel.addComponent(panel)

        var element = panel.createUIElement(width, height, false)
        element.addPara("Member: ${member.shipName}", 0f)
        panel.addUIElement(element)*/

        MusicController.startMusic()

        ShipModLoader.get(member, variant) ?: ShipModLoader.set(member, variant, ShipModFactory.generateForFleetMember(member))

        var plugin = ShipModUIPlugin(Global.getSector().campaignUI.currentInteractionDialog, backgroundPanel, width, height)
        var modPanel = plugin.layoutPanels()
        modPanel.position.inTL(-1f, 0f)
        plugin.showPanel(member, variant)

        backgroundPanel.position.inTL(x, y)

        //Call this whenever you need to close the panel.
        //backgroundPlugin.close()

        //Set to true whenever you make a change that needs to be reflected in the refit screen, like adding a hullmod or a stat change.
        //RefitButtonAdder.requiresVariantUpdate = true
    }



}