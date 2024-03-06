package exoticatechnologies.ui2.impl.mods.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.modifications.exotics.types.ExoticTypeTooltip
import exoticatechnologies.refit.RefitButtonAdder
import exoticatechnologies.ui2.impl.mods.exotics.methods.ChipMethod
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.mods.ExoticaPanel
import exoticatechnologies.ui2.impl.resources.ResourcePanelContext
import exoticatechnologies.ui2.impl.resources.ResourcesPanel
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.safeLet
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class ExoticItemPanel(val exotic: Exotic, context: ExoticaPanelContext) : ExoticaPanel(context) {
    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        safeLet(member, variant, mods) { member, variant, mods ->
            val tooltip = menuPanel.createUIElement(innerWidth * 3f / 5f, innerHeight, false)
            val exoticData = mods.getExoticData(exotic) ?: ExoticData(exotic)

            tooltip.setParaOrbitronLarge()
            tooltip.addPara(exotic.name, exotic.color, 0f)
            if (exoticData.type != ExoticType.NORMAL) {
                tooltip.setParaFontVictor14()

                var typeText = exoticData.type.name
                if (!mods.hasExotic(exotic) || mods.getExoticData(exotic)!!.type != exoticData.type) {
                    typeText = StringUtils.getTranslation("ExoticTypes", "NotInstalledText")
                        .format("typeName", typeText)
                        .toStringNoFormats()
                }
                tooltip.addPara(typeText, exoticData.type.colorOverlay.setAlpha(255), 0f)
                ExoticTypeTooltip.addToPrev(tooltip, member, mods, exoticData.type)
            }

            tooltip.setParaFontDefault()

            exotic.printDescriptionToTooltip(tooltip, member)
            tooltip.addPara("", 3f)
            exotic.modifyToolTip(tooltip, tooltip.prev, member, mods, exoticData, true)

            menuPanel.addUIElement(tooltip).inTL(innerPadding, innerPadding)

            val resourceContext = ResourcePanelContext()
            resourceContext.copy(context)
            val resourcesPanel = ResourcesPanel(resourceContext)
            resourcesPanel.panelWidth = innerWidth * 2f / 5f
            resourcesPanel.panelHeight = innerHeight * 2f / 3f
            resourcesPanel.layoutPanel(menuPanel, null).position.inTR(innerPadding, innerPadding)

            val exoticMethodsPanel = ExoticMethodsPanel(exotic, context)
            exoticMethodsPanel.panelWidth = innerWidth * 2f / 5f
            exoticMethodsPanel.panelHeight = innerHeight / 3f
            exoticMethodsPanel.layoutPanel(menuPanel, null).position.inBR(innerPadding, innerPadding)

            var chipListOpen = false
            exoticMethodsPanel.onMethodClicked { method ->
                if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT) {
                    RefitButtonAdder.requiresVariantUpdate = true
                }

                if (method is ChipMethod) {
                    if (!chipListOpen) {
                        chipListOpen = true
                        val chipListPanel = ExoticChipListPanel(ExoticChipListContext(exotic, currContext))
                        chipListPanel.panelWidth = innerWidth * 2f / 5f
                        chipListPanel.panelHeight = innerHeight * 2f / 3f
                        chipListPanel.itemWidth = innerWidth * 2f / 5f - 8f
                        chipListPanel.renderBackground = true
                        chipListPanel.bgColor = Color.BLACK
                        chipListPanel.layoutPanel(menuPanel, null).position.inTR(innerPadding, innerPadding)
                        chipListPanel.addListener { itemContext ->
                            method.chipStack = itemContext.item
                            method.apply(member, variant, mods, exotic, market)
                            Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f)
                            refreshPanel()
                        }
                    } else {
                        refreshPanel()
                    }
                } else {
                    method.apply(member, variant, mods, exotic, market)
                    Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f)
                    refreshPanel()
                }
            }

            exoticMethodsPanel.onMethodHighlighted {
                if (chipListOpen) return@onMethodHighlighted
                resourceContext.resourceCosts.clear()

                it.getResourceMap(member, mods, exotic, market, true)?.let { resourceCosts ->
                    resourceContext.resourceCosts.putAll(resourceCosts)
                }

                if (!it.canUseIfMarketIsNull() && market == null) {
                    resourceContext.resourceCosts["^CommonOptions.MustBeDockedAtMarket"] = 1f
                }

                resourcesPanel.refreshPanel().position.inTR(innerPadding, innerPadding)
            }

            exoticMethodsPanel.onMethodUnhighlighted {
                if (chipListOpen) return@onMethodUnhighlighted
                resourceContext.resourceCosts.clear()

                resourcesPanel.refreshPanel().position.inTR(innerPadding, innerPadding)
            }
        }
    }
}