package exoticatechnologies.ui2.impl.crafting

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.crafting.Recipe
import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.ui2.BasePanelContext
import exoticatechnologies.ui2.RefreshablePanel
import java.awt.Color

class RecipeOutputPreviewPanel(context: RecipeOutputPreviewPanelContext) :
    RefreshablePanel<RecipeOutputPreviewPanelContext>(context) {

    override var renderBorder: Boolean = true
    override var outlineColor: Color = Color.RED

    private val recipe: Recipe
        get() = currContext.recipePanelContext.recipe
    private val pickedIngredientsBySpec: List<List<Ingredient>>
        get() = currContext.recipePanelContext.pickedIngredientsBySpec


    override fun refresh(menuPanel: CustomPanelAPI, context: RecipeOutputPreviewPanelContext) {
        val tooltip = menuPanel.createUIElement(innerWidth, 24f, false)
        tooltip.setTitleOrbitronLarge()
        tooltip.addTitle("Output", Misc.getBasePlayerColor())
        menuPanel.addUIElement(tooltip).inTMid(0f)

        val outputTooltip =
            menuPanel.createUIElement(innerWidth, innerHeight - innerPadding * 3f - 32f, false)
        recipe.modifyOutputPreviewTooltip(
            outputTooltip,
            innerWidth - innerPadding * 2f,
            innerHeight - 24f - innerPadding,
            pickedIngredientsBySpec
        )
        menuPanel.addUIElement(outputTooltip).belowMid(tooltip, innerPadding)

        val craftButtonPanel = RefreshablePanel(BasePanelContext())
        val actualPanel = craftButtonPanel.layoutPanel(menuPanel, null)
        actualPanel.position.belowMid(outputTooltip, innerPadding).setXAlignOffset(-innerPadding)

        val craftButtonTooltip = actualPanel.createUIElement(innerWidth, 32f, false)
        val craftButton = craftButtonTooltip.addButton("CRAFT", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), innerWidth, 32f, 0f)
        if (!recipe.canCraft(currContext.recipePanelContext.pickedIngredientsBySpec)) {
            craftButton.setClickable(false)
            craftButton.isEnabled = false
            craftButton.isPerformActionWhenDisabled = false
        }
        actualPanel.addUIElement(craftButtonTooltip).inMid()

        craftButtonPanel.onClick(craftButton) {
            currContext.recipePanel.recipeCraftButtonClicked()
        }
    }
}

open class RecipeOutputPreviewPanelContext(
    val recipePanelContext: RecipePanelContext,
    val recipePanel: RecipePanel
) : CraftingPanelContext(recipePanelContext.fleet, recipePanelContext.market) {

}