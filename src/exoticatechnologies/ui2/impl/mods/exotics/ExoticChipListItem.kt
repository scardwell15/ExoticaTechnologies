package exoticatechnologies.ui2.impl.mods.exotics

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.list.ListItem
import exoticatechnologies.ui2.list.ListItemContext
import exoticatechnologies.ui2.list.ListPanelContext
import exoticatechnologies.util.getFleetModuleSafe
import exoticatechnologies.util.safeLet
import org.magiclib.kotlin.setAlpha

class ExoticChipListItem(context: ExoticChipListItemContext) : ListItem<CargoStackAPI>(context) {
    override fun decorate(menuPanel: CustomPanelAPI) {
        (currContext as? ExoticChipListItemContext)?.let { context ->
            val plugin = getPlugin()
            safeLet(context.member, context.mods, plugin.exoticData) { member, mods, exoticData ->
                val exotic = exoticData.exotic
                val type = exoticData.type

                val iconSize = innerHeight *  0.75f
                val itemImage = menuPanel.createUIElement(iconSize, iconSize, false)
                itemImage.addImage(plugin.spec.iconName, iconSize, 0f)
                menuPanel.addUIElement(itemImage).inLMid(0f)

                val itemInfo = menuPanel.createUIElement(panelWidth - iconSize, panelHeight, false)
                val nameLabel = itemInfo.addPara(exotic.name, exoticData.getColor(), 0f)
                nameLabel.position.inTL(0f, panelHeight / 2f - nameLabel.position.height)
                val nameElement = itemInfo.prev

                itemInfo.addPara(type.name, type.colorOverlay.setAlpha(255), 0f).position.belowLeft(nameElement, innerPadding)
                menuPanel.addUIElement(itemInfo).rightOfMid(itemImage, innerPadding * 2f)
            }
        }
    }

    private fun getPlugin(): ExoticSpecialItemPlugin {
        return item.plugin as ExoticSpecialItemPlugin
    }
}

class ExoticChipListItemContext(item: CargoStackAPI, val exoticaContext: ExoticaPanelContext) :
    ListItemContext<CargoStackAPI>(item) {
    val member: FleetMemberAPI?
        get() = exoticaContext.member
    val variant: ShipVariantAPI?
        get() = exoticaContext.variant
    val mods: ShipModifications?
        get() = exoticaContext.mods
    val fleet: CampaignFleetAPI?
        get() = exoticaContext.member?.getFleetModuleSafe()
    val market: MarketAPI?
        get() = exoticaContext.market

    override fun createListItem(listContext: ListPanelContext<CargoStackAPI>): ListItem<CargoStackAPI> {
        return ExoticChipListItem(this)
    }
}
