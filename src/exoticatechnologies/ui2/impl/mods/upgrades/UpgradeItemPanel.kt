package exoticatechnologies.ui2.impl.mods.upgrades

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.refit.RefitButtonAdder
import exoticatechnologies.ui2.impl.mods.upgrades.methods.ChipMethod
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.mods.ExoticaPanel
import exoticatechnologies.ui2.impl.resources.ResourcePanelContext
import exoticatechnologies.ui2.impl.resources.ResourcesPanel
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.safeLet
import java.awt.Color

class UpgradeItemPanel(val upgrade: Upgrade, context: ExoticaPanelContext) : ExoticaPanel(context) {
    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        safeLet(member, variant, mods) { member, variant, mods ->
            val tooltip = menuPanel.createUIElement(innerWidth * 3f / 5f, innerHeight, false)

            val color: Color = upgrade.color
            tooltip.setParaOrbitronLarge()
            tooltip.addPara(upgrade.name, color, 0f)
            tooltip.setParaFontDefault()

            val levelText = StringUtils.getTranslation("Upgrades", "UpgradeLevel")
                .format("level", mods.getUpgrade(upgrade))
                .toStringNoFormats()
            tooltip?.addPara(levelText, color, 3f)

            upgrade.showDescriptionInShop(tooltip, member, mods)
            tooltip!!.addPara("", color, 3f)
            upgrade.showStatsInShop(tooltip, member, mods)

            menuPanel.addUIElement(tooltip).inTL(innerPadding, innerPadding)

            val resourceContext = ResourcePanelContext()
            resourceContext.copy(context)
            val resourcesPanel = ResourcesPanel(resourceContext)
            resourcesPanel.panelWidth = innerWidth * 2f / 5f
            resourcesPanel.panelHeight = innerHeight * 2f / 3f
            resourcesPanel.layoutPanel(menuPanel, null).position.inTR(innerPadding, innerPadding)

            val upgradeMethodsPanel = UpgradeMethodsPanel(upgrade, context)
            upgradeMethodsPanel.panelWidth = innerWidth * 2f / 5f
            upgradeMethodsPanel.panelHeight = innerHeight / 3f
            upgradeMethodsPanel.layoutPanel(menuPanel, null).position.inBR(innerPadding, innerPadding)

            var chipListOpen = false
            upgradeMethodsPanel.onMethodClicked { method ->
                if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT) {
                    RefitButtonAdder.requiresVariantUpdate = true
                }

                if (method is ChipMethod) {
                    if (!chipListOpen) {
                        chipListOpen = true
                        val chipListPanel = UpgradeChipListPanel(UpgradeChipListContext(upgrade, currContext))
                        chipListPanel.panelWidth = innerWidth * 2f / 5f
                        chipListPanel.panelHeight = innerHeight * 2f / 3f
                        chipListPanel.itemWidth = innerWidth * 2f / 5f - 8f
                        chipListPanel.renderBackground = true
                        chipListPanel.bgColor = Color.BLACK
                        chipListPanel.layoutPanel(menuPanel, null).position.inTR(innerPadding, innerPadding)
                        chipListPanel.addListener { itemContext ->
                            method.upgradeChipStack = itemContext.item
                            method.apply(member, variant, mods, upgrade, market)
                            Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f)
                            refreshPanel()
                        }
                    } else {
                        refreshPanel()
                    }
                } else {
                    method.apply(member, variant, mods, upgrade, market)
                    Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f)
                    refreshPanel()
                }
            }

            upgradeMethodsPanel.onMethodHighlighted {
                if (chipListOpen) return@onMethodHighlighted
                val resourceCosts = it.getResourceCostMap(member, mods, upgrade, market, true)
                resourceContext.resourceCosts.clear()
                resourceContext.resourceCosts.putAll(resourceCosts)

                if (!it.canUseIfMarketIsNull() && market == null) {
                    resourceContext.resourceCosts["^CommonOptions.MustBeDockedAtMarket"] = 1f
                }

                resourcesPanel.refreshPanel().position.inTR(innerPadding, innerPadding)
            }

            upgradeMethodsPanel.onMethodUnhighlighted {
                if (chipListOpen) return@onMethodUnhighlighted
                resourceContext.resourceCosts.clear()

                resourcesPanel.refreshPanel().position.inTR(innerPadding, innerPadding)
            }
        }
    }
}