package exoticatechnologies.ui.impl.shop.overview

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.CutStyle
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.refit.RefitButtonAdder
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.ButtonHandler
import exoticatechnologies.ui.impl.shop.ShopMenuUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.RecoverMethod
import exoticatechnologies.ui.tabs.TabButtonUIPlugin
import exoticatechnologies.ui.tabs.TabbedPanelUIPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class OverviewPanelUIPlugin: ShopMenuUIPlugin() {
    val pad: Float = 3f
    val opad: Float = 10f
    override var bgColor: Color = Color(200, 180, 40, 0)
    override val tabText: String = "Overview"

    private var mainPanel: CustomPanelAPI? = null
    private var innerPanel: CustomPanelAPI? = null

    private var expandUpgrades: Boolean = false
    private var expandExotics: Boolean = false


    override fun getNewTabButtonUIPlugin(): TabButtonUIPlugin {
        return OverviewTabUIPlugin()
    }

    override fun layoutPanel(holdingPanel: CustomPanelAPI, parentPlugin: TabbedPanelUIPlugin): TooltipMakerAPI? {
        val tooltip = holdingPanel.createUIElement(panelWidth, panelHeight, false)
        val panel = holdingPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        showPanel(panel)

        tooltip.addCustom(panel, 0f).position.inTL(pad, pad)
        holdingPanel.addUIElement(tooltip)

        return tooltip
    }

    fun showPanel(panel: CustomPanelAPI) {
        innerPanel?.let {
            panel.removeComponent(it)
            innerPanel = null
        }

        val panelPlugin = BaseUIPanelPlugin()
        panelPlugin.bgColor = Color(255, 0, 0, 0)
        innerPanel = panel.createCustomPanel(panelWidth, panelHeight, panelPlugin)

        val buttonTooltip = innerPanel!!.createUIElement(panelWidth, 25f, false)
        buttonTooltip.setParaFontOrbitron()

        val expandUpgradesButton = buttonTooltip.addButton(StringUtils.getString("OverviewDialog", "ExpandUpgrades"), null,
            Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.C2_MENU, 120f, 22F, 3F)
        expandUpgradesButton.position.inTL(3f, 3f)
        buttons[expandUpgradesButton] = ExpandUpgradesButtonHandler()

        val expandExoticsButton = buttonTooltip.addButton(StringUtils.getString("OverviewDialog", "ExpandExotics"), null,
            Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.C2_MENU, 120F, 22F, 3F)
        expandExoticsButton.position.rightOfMid(expandUpgradesButton, 3f)
        buttons[expandExoticsButton] = ExpandExoticsButtonHandler()

        val upgradeRecoverPrice = mods!!.upgrades.map.keys
            .mapNotNull { UpgradesHandler.UPGRADES[it] }
            .sumOf { RecoverMethod.getCreditCost(member!!, mods!!, it) }
            .toFloat()

        val upgradeRecoverPriceText = StringUtils.getTranslation("OverviewDialog", "ClearUpgrades")
            .format("creditsText", Misc.getDGSCredits(upgradeRecoverPrice))
            .toStringNoFormats()

        val clearUpgradesButton = buttonTooltip.addButton(upgradeRecoverPriceText, null,
            Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.C2_MENU, buttonTooltip.computeStringWidth(upgradeRecoverPriceText) + 10f, 22F, 3F)
        clearUpgradesButton.position.rightOfMid(expandExoticsButton, 3f)
        clearUpgradesButton.isEnabled = upgradeRecoverPrice != 0f && market != null
        buttons[clearUpgradesButton] = ClearUpgradesButtonHandler()

        val exoticRecoverPrice = mods!!.exotics.list.size
        val exoticRecoverPriceText = StringUtils.getTranslation("OverviewDialog", "ClearExotics")
            .format("storyPoints", exoticRecoverPrice)
            .toStringNoFormats()

        val clearExoticsButton = buttonTooltip.addButton(exoticRecoverPriceText, null,
            Misc.getStoryOptionColor(), Misc.getStoryDarkColor(), Alignment.MID, CutStyle.C2_MENU, buttonTooltip.computeStringWidth(exoticRecoverPriceText) + 10f, 22F, 3F)
        clearExoticsButton.position.rightOfMid(clearUpgradesButton, 3f)
        clearExoticsButton.isEnabled = exoticRecoverPrice != 0 && market != null
        buttons[clearExoticsButton] = ClearExoticsButtonHandler()

        innerPanel!!.addUIElement(buttonTooltip).inTL(0f, 0f)

        val statsTooltip = innerPanel!!.createUIElement(panelWidth, panelHeight - 56f, false)
        mods!!.populateTooltip(member!!, statsTooltip, panelWidth, panelHeight - 56f, expandUpgrades, expandExotics, false)
        innerPanel!!.addUIElement(statsTooltip).belowMid(buttonTooltip, -70f)

        panel.addComponent(innerPanel).inTL(0f, 0f)
    }

    inner class ExpandUpgradesButtonHandler: ButtonHandler() {
        override fun checked() {
            this@OverviewPanelUIPlugin.apply {
                expandUpgrades = !expandUpgrades
                showPanel(mainPanel!!)
            }
        }
    }

    inner class ExpandExoticsButtonHandler: ButtonHandler() {
        override fun checked() {
            this@OverviewPanelUIPlugin.apply {
                expandExotics = !expandExotics
                showPanel(mainPanel!!)
            }
        }
    }

    inner class ClearUpgradesButtonHandler: ButtonHandler() {
        override fun checked() {
            this@OverviewPanelUIPlugin.apply {
                mods!!.upgrades.map.keys
                    .mapNotNull { UpgradesHandler.UPGRADES[it] }
                    .forEach {
                        RecoverMethod().apply(member!!, variant!!, mods!!, it, market!!)
                    }

                if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT) {
                    RefitButtonAdder.requiresVariantUpdate = true
                }

                showPanel(mainPanel!!)
            }
        }
    }

    inner class ClearExoticsButtonHandler: ButtonHandler() {
        override fun checked() {
            this@OverviewPanelUIPlugin.apply {
                mods!!.exotics.list
                    .map { ExoticsHandler.EXOTICS[it] }
                    .forEach {
                        exoticatechnologies.ui.impl.shop.exotics.methods.RecoverMethod().apply(
                            member!!,
                            variant!!,
                            mods!!,
                            it!!,
                            market!!
                        )
                    }

                if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT) {
                    RefitButtonAdder.requiresVariantUpdate = true
                }

                showPanel(mainPanel!!)
            }
        }
    }

    class OverviewTabUIPlugin: TabButtonUIPlugin(StringUtils.getString("OverviewDialog", "OverviewTabText")) {
        override var panelWidth = 100f

        override val activeColor: Color = Color(215, 230, 230, 255)
        override val baseColor: Color = Color(120, 130, 130, 255)
    }
}