package exoticatechnologies.ui2.impl.crafting

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.ExoticaTabContext
import java.awt.Color
import java.lang.Math.min

class CraftingPanel(context: ExoticaPanelContext) : RefreshablePanel<ExoticaPanelContext>(context) {
    override var outerPadding: Float = 4f

    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        val titleTooltip = menuPanel.createUIElement(innerWidth, 22f, false)
        titleTooltip.setParaOrbitronLarge()
        titleTooltip.addPara("Crafterino", innerPadding)
        menuPanel.addUIElement(titleTooltip).inTL(innerPadding, innerPadding)

        context.fleet?.let {
            val craftingContext = CraftingPanelContext(it, context.market)
            val listHeight = innerHeight - innerPadding - 22f
            val listHolder = menuPanel.createCustomPanel(innerWidth, listHeight, null)
            val recipesList = RecipesListPanel(RecipesListContext(craftingContext))
            recipesList.itemWidth = recipesList.itemWidth.coerceAtMost(Global.getSettings().screenWidth / 10f)
            recipesList.panelWidth = innerWidth
            recipesList.panelHeight = innerHeight - innerPadding - 22f
            recipesList.layoutPanel(listHolder, null).position.inTL(innerPadding, innerPadding)
            menuPanel.addComponent(listHolder).belowLeft(titleTooltip, innerPadding)
        }
    }
}

class CraftingPanelTabContext :
    ExoticaTabContext(CraftingPanel(ExoticaPanelContext()), "crafting", "Crafting", Color(100, 220, 130, 255)) {
    override val highlightedColor: Color
        get() = Color(150, 240, 180, 255)
}

open class CraftingPanelContext(val fleet: CampaignFleetAPI, val market: MarketAPI?) : PanelContext