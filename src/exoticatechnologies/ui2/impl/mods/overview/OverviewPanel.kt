package exoticatechnologies.ui2.impl.mods.overview

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.CutStyle
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.refit.RefitButtonAdder
import exoticatechnologies.ui2.impl.mods.upgrades.methods.RecoverMethod
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.mods.ExoticaPanel
import exoticatechnologies.ui2.impl.mods.ModTabContext
import exoticatechnologies.util.StringUtils
import java.awt.Color

class OverviewPanel(context: ExoticaPanelContext) : ExoticaPanel(context) {
    override var bgColor: Color = Color(200, 180, 40, 0)
    private var expandUpgrades: Boolean = false
    private var expandExotics: Boolean = false

    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        val buttonTooltip = menuPanel.createUIElement(panelWidth, 25f, false)
        buttonTooltip.setParaFontOrbitron()

        val expandUpgradesButton = buttonTooltip.addButton(
            StringUtils.getString("OverviewDialog", "ExpandUpgrades"), null,
            Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.C2_MENU, 120f, 22F, 3F
        )
        expandUpgradesButton.position.inTL(3f, 3f)
        onClick(expandUpgradesButton) {
            expandUpgrades = !expandUpgrades
            refreshPanel()
        }

        val expandExoticsButton = buttonTooltip.addButton(
            StringUtils.getString("OverviewDialog", "ExpandExotics"), null,
            Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.C2_MENU, 120F, 22F, 3F
        )
        expandExoticsButton.position.rightOfMid(expandUpgradesButton, 3f)
        onClick(expandExoticsButton) {
            expandExotics = !expandExotics
            refreshPanel()
        }

        val upgradeRecoverPrice = mods!!.upgrades.map.keys
            .mapNotNull { UpgradesHandler.UPGRADES[it] }
            .sumOf { RecoverMethod.getCreditCost(member!!, mods!!, it) }
            .toFloat()

        val upgradeRecoverPriceText = StringUtils.getTranslation("OverviewDialog", "ClearUpgrades")
            .format("creditsText", Misc.getDGSCredits(upgradeRecoverPrice))
            .toStringNoFormats()

        val clearUpgradesButton = buttonTooltip.addButton(
            upgradeRecoverPriceText,
            null,
            Misc.getBasePlayerColor(),
            Misc.getDarkPlayerColor(),
            Alignment.MID,
            CutStyle.C2_MENU,
            buttonTooltip.computeStringWidth(upgradeRecoverPriceText) + 10f,
            22F,
            3F
        )
        clearUpgradesButton.position.rightOfMid(expandExoticsButton, 3f)
        clearUpgradesButton.isEnabled = upgradeRecoverPrice != 0f && market != null
        onClick(clearUpgradesButton) {
            mods?.let { mods ->
                mods.upgrades.map.keys
                    .mapNotNull { UpgradesHandler.UPGRADES[it] }
                    .forEach {
                        RecoverMethod().apply(member!!, variant!!, mods, it, market!!)
                    }

                if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT) {
                    RefitButtonAdder.requiresVariantUpdate = true
                }

                refreshPanel()
            }
        }

        val exoticRecoverPrice = mods!!.exotics.list.size
        val exoticRecoverPriceText = StringUtils.getTranslation("OverviewDialog", "ClearExotics")
            .format("storyPoints", exoticRecoverPrice)
            .toStringNoFormats()

        val clearExoticsButton = buttonTooltip.addButton(
            exoticRecoverPriceText,
            null,
            Misc.getStoryOptionColor(),
            Misc.getStoryDarkColor(),
            Alignment.MID,
            CutStyle.C2_MENU,
            buttonTooltip.computeStringWidth(exoticRecoverPriceText) + 10f,
            22F,
            3F
        )
        clearExoticsButton.position.rightOfMid(clearUpgradesButton, 3f)
        clearExoticsButton.isEnabled = exoticRecoverPrice != 0 && market != null
        onClick(clearExoticsButton) {
            mods?.let { mods ->
                mods.exotics.list
                    .map { ExoticsHandler.EXOTICS[it] }
                    .forEach {
                        exoticatechnologies.ui2.impl.mods.exotics.methods.RecoverMethod().apply(
                            member!!,
                            variant!!,
                            mods,
                            it!!,
                            market
                        )
                    }

                if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT) {
                    RefitButtonAdder.requiresVariantUpdate = true
                }

                refreshPanel()
            }
        }

        menuPanel.addUIElement(buttonTooltip).inTL(0f, 0f)

        val statsTooltip = menuPanel.createUIElement(panelWidth, panelHeight - 56f, false)
        mods!!.populateTooltip(
            member!!,
            statsTooltip,
            panelWidth,
            panelHeight - 56f,
            expandUpgrades,
            expandExotics,
            false
        )
        menuPanel.addUIElement(statsTooltip).belowMid(buttonTooltip, -70f)
    }
}

class OverviewTabContext() : ModTabContext(
    OverviewPanel(ExoticaPanelContext()),
    "overview",
    StringUtils.getString("OverviewDialog", "OverviewTabText"),
    Color(120, 130, 130, 255)
)