package exoticatechnologies.ui2.impl.crafting

import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.crafting.Recipe
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.TimedFadingPanel

open class RecipeResultPanel(endLife: Float, currContext: RecipeResultContext): TimedFadingPanel<RecipeResultContext>(endLife, currContext) {
    override fun refresh(menuPanel: CustomPanelAPI, context: RecipeResultContext) {
        val tooltip = menuPanel.createUIElement(panelWidth, panelHeight, false)
        context.recipe.modifyPostCraftingOutputTooltip(
            this,
            tooltip,
            innerWidth,
            innerHeight,
            context.craftingOutputData
        )
        menuPanel.addUIElement(tooltip).inMid()
    }
}

class RecipeResultContext(val recipe: Recipe, val craftingOutputData: Any?): PanelContext