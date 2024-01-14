package exoticatechnologies.ui.impl.shop.upgrades.chips

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipListUIPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipSearcher
import java.awt.Color

class UpgradeChipPanelUIPlugin(
    parentPanel: CustomPanelAPI,
    val upgrade: Upgrade,
    member: FleetMemberAPI,
    variant: ShipVariantAPI,
    mods: ShipModifications,
    market: MarketAPI
) : ChipPanelUIPlugin<UpgradeSpecialItemPlugin>(parentPanel, upgrade, member, variant, mods, market) {
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun getChipSearcher(): ChipSearcher<UpgradeSpecialItemPlugin> {
        return UpgradeChipSearcher()
    }

    override fun getChipListPlugin(listPanel: CustomPanelAPI): ChipListUIPlugin {
        return UpgradeChipListUIPlugin(parentPanel, member, variant, mods)
    }
}