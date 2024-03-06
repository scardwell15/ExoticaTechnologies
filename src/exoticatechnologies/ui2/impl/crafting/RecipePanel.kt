package exoticatechnologies.ui2.impl.crafting

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.crafting.Recipe
import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.crafting.ingredients.spec.IngredientSpec
import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.list.ListPanel
import exoticatechnologies.util.forEachIndexedReversed
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class RecipePanel(context: RecipePanelContext) : RefreshablePanel<RecipePanelContext>(context) {
    private val recipe: Recipe
        get() = currContext.recipe
    val recipeIngredientSelectors: MutableList<RecipeIngredientPanel> = mutableListOf()
    var recipeIngredientHolder: CustomPanelAPI? = null
    var ingredientSelectorHolder: CustomPanelAPI? = null
    var inputTitleTooltip: TooltipMakerAPI? = null
    var recipeOutputPreviewPanel: RecipeOutputPreviewPanel? = null
    val ingredientsHeight: Float
        get() = innerHeight - 80f - innerPadding
    var selectedSpecIndex: Int
        get() = currContext.selectedSpecIndex
        set(value) {
            currContext.selectedSpecIndex = value
        }

    override fun refresh(menuPanel: CustomPanelAPI, context: RecipePanelContext) {
        recipeIngredientSelectors.clear()

        val tooltip = menuPanel.createUIElement(innerWidth * 3f / 5f, 50f, false)
        val color: Color = recipe.getTextColor()
        tooltip.setParaOrbitronVeryLarge()
        tooltip.addPara(recipe.getName(), color, 0f)

        tooltip.setParaFontDefault()
        tooltip.addPara(recipe.getDescription(), 0f)
        menuPanel.addUIElement(tooltip).inTL(innerPadding, innerPadding)

        // Ingredients
        val inputPanelWidth = innerWidth * 3f / 5f
        val inputTooltip = menuPanel.createUIElement(inputPanelWidth, 30f, false)
        inputTitleTooltip = inputTooltip
        inputTooltip.setTitleOrbitronLarge()
        inputTooltip.addTitle("Inputs - Click to pick ingredients")
        menuPanel.addUIElement(inputTooltip).belowLeft(tooltip, innerPadding)

        val ingredientHolder = menuPanel.createCustomPanel(inputPanelWidth / 2f - 4f, ingredientsHeight, null)
        recipeIngredientHolder = ingredientHolder
        menuPanel.addComponent(ingredientHolder).belowLeft(inputTooltip, innerPadding)

        val selectorHolder = menuPanel.createCustomPanel(inputPanelWidth / 2f - innerPadding * 2f, ingredientsHeight, null)
        ingredientSelectorHolder = selectorHolder
        menuPanel.addComponent(selectorHolder).rightOfTop(ingredientHolder, innerPadding).setYAlignOffset(1f)

        //update specs before populating output panel
        val ingredientSpecs = context.updateSpecs()

        val outputPanel = RecipeOutputPreviewPanel(RecipeOutputPreviewPanelContext(this.currContext, this))
        recipeOutputPreviewPanel = outputPanel
        outputPanel.panelWidth = innerWidth * 2f / 5f
        outputPanel.panelHeight = ingredientsHeight + 30f
        outputPanel.layoutPanel(menuPanel, null).position.rightOfTop(inputTooltip, innerPadding)

        for (index in ingredientSpecs.indices) {
            val ingredientSpec = ingredientSpecs[index]
            this@RecipePanel.currContext.filterForNewIngredientSpecs(index, ingredientSpec)

            val recipeIngredientContext = RecipeIngredientPanelContext(this, context, ingredientSpec, index)
            recipeIngredientContext.setSelectedIngredients(this.currContext.pickedIngredientsBySpec[index])

            val recipeIngredientPanel = RecipeIngredientPanel(recipeIngredientContext)
            recipeIngredientPanel.panelWidth = inputPanelWidth / 2f - 4f
            recipeIngredientPanel.panelHeight = 80f

            recipeIngredientSelectors.add(recipeIngredientPanel)
            autoselectRecipeIngredientIfPossible(index, ingredientSpec)

            recipeIngredientPanel.layoutPanel(ingredientHolder, null)

            if (currContext.selectedSpecIndex == index) {
                //add listener for picking any ingredient
                val ingredientSelectorList =
                    InputIngredientSelectionList(InputIngredientSelectionListContext(recipeIngredientContext, false)).apply {
                        panelWidth = inputPanelWidth / 2f - innerPadding * 2f
                        panelHeight = ingredientsHeight
                        itemWidth = panelWidth
                        itemHeight = 80f
                        outlineColor = Color.ORANGE
                        bgColor = Color.ORANGE.setAlpha(50)
                        renderBackground = true
                    }

                val listPanel = ingredientSelectorList.layoutPanel(selectorHolder, null)
                //listPanel.position.inTMid(innerPadding)

                addIngredientSelectorListListener(index, ingredientSpec, ingredientSelectorList)
            }
        }
    }

    fun addRecipeSelectorHook(recipeIngredientPanel: RecipeIngredientPanel) {
        val index = recipeIngredientPanel.currContext.index
        recipeIngredientPanel.lunaElement?.onClick {
            if (selectedSpecIndex == index) {
                currContext.selectedSpecIndex = -1
            } else {
                currContext.selectedSpecIndex = index
            }
            this@RecipePanel.refreshPanel()
        }
    }

    fun addIngredientSelectorListListener(
        index: Int,
        ingredientSpec: IngredientSpec<out Ingredient>,
        ingredientSelectorList: ListPanel<Ingredient>
    ) {
        ingredientSelectorList.addListener { newIngredientItem ->
            val selectedIngredients = currContext.pickedIngredientsBySpec[index]
            val recipeRequirementsMet = ingredientSpec.hasRequiredAmount(selectedIngredients)
            val lastIngredient: Ingredient? = selectedIngredients.lastOrNull()
            val newIngredient = newIngredientItem.item

            val isIngredientPicked =
                currContext.pickIngredient(index, newIngredient)
            if (isIngredientPicked) {
                //if recipeRequirements already met, remove either the ingredient with the least resources or the last ingredient.
                if (recipeRequirementsMet) {
                    val ingredientToRemove: Ingredient? =
                        selectedIngredients.filter { it != newIngredient }.minByOrNull { it.getQuantity() }
                            ?: lastIngredient
                    ingredientToRemove?.let {
                        selectedIngredients.remove(ingredientToRemove)
                    }
                }
            }


            val newIngredientSpecsAfterSelection = currContext.updateSpecs()
            recipeIngredientSelectors.forEachIndexedReversed { index, otherRecipeIngredientPanel ->
                val selectedIngredientsAfterUpdate = currContext.pickedIngredientsBySpec[index]
                otherRecipeIngredientPanel.currContext.ingredientSpec = newIngredientSpecsAfterSelection[index]
                otherRecipeIngredientPanel.currContext.setSelectedIngredients(selectedIngredientsAfterUpdate)
                if (currContext.selectedSpecIndex != otherRecipeIngredientPanel.currContext.index) {
                    autoselectRecipeIngredientIfPossible(
                        otherRecipeIngredientPanel.currContext.index,
                        newIngredientSpecsAfterSelection[index]
                    )
                }
                otherRecipeIngredientPanel.refreshPanel()
            }

            recipeOutputPreviewPanel?.refreshPanel()?.position?.rightOfTop(inputTitleTooltip, this.innerPadding)
        }
    }

    fun autoselectRecipeIngredientIfPossible(index: Int, ingredientSpec: IngredientSpec<out Ingredient>) {
        val previewedIngredients = ingredientSpec.createIngredients(currContext.fleet, currContext.market)
        if (previewedIngredients.size == 1) {
            val onlyIngredient = previewedIngredients.first()
            if (currContext.hasIngredientForSpec(index, ingredientSpec, onlyIngredient)) {
                currContext.pickedIngredientsBySpec[index].clear()
                currContext.pickIngredient(index, onlyIngredient)
                recipeIngredientSelectors[index].currContext.setSelectedIngredients(currContext.pickedIngredientsBySpec[index])
            }
        }
    }

    fun recipeCraftButtonClicked() {
        recipe.takeIngredients(currContext.fleet, currContext.market, currContext.pickedIngredientsBySpec)

        val recipeCraftData: Any? = recipe.crafted(
            currContext.fleet,
            currContext.market,
            currContext.pickedIngredientsBySpec,
            recipeOutputPreviewPanel?.currContext
        )

        this.refreshPanel()

        recipeOutputPreviewPanel?.let { recipeOutputPreviewPanel ->
            recipe.postCraft(recipeOutputPreviewPanel.currContext, recipeOutputPreviewPanel, recipeCraftData)
        }
    }

    fun recipeSelectorRefreshed(ingredientPanel: RecipeIngredientPanel) {
        recipeIngredientSelectors.forEachIndexed { index, recipeIngredientPanel ->
            recipeIngredientPanel.getPanel()?.let {
                if (index == 0) {
                    it.position.inTL(innerPadding, innerPadding)
                } else {
                    val lastElement = recipeIngredientSelectors[index - 1].getPanel()
                    it.position.belowLeft(lastElement, innerPadding * 2f)
                }
            }
        }
        addRecipeSelectorHook(ingredientPanel)
    }
}

open class RecipePanelContext(val recipe: Recipe, fleet: CampaignFleetAPI, market: MarketAPI?) :
    CraftingPanelContext(fleet, market) {
    var specs = mutableListOf<IngredientSpec<out Ingredient>>()
    var pickedIngredientsBySpec = mutableListOf<MutableList<Ingredient>>()
    var selectedSpecIndex: Int = -1

    init {
        specs = recipe.getIngredientSpecs(pickedIngredientsBySpec).toMutableList()
        for (i in 0 until specs.size) {
            if (pickedIngredientsBySpec.size <= i) {
                pickedIngredientsBySpec.add(mutableListOf())
            }
        }
    }

    fun updateSpecs(): List<IngredientSpec<out Ingredient>> {
        specs = recipe.getIngredientSpecs(pickedIngredientsBySpec).toMutableList()
        specs.forEachIndexed { index, ingredientSpec ->
            filterForNewIngredientSpecs(index, ingredientSpec)
        }
        return specs
    }

    fun pickIngredient(index: Int, ingredient: Ingredient): Boolean {
        val returnValue: Boolean
        if (pickedIngredientsBySpec[index].removeAll { it == ingredient }) {
            returnValue = false
        } else {
            pickedIngredientsBySpec[index].add(ingredient)
            returnValue = true
        }

        return returnValue
    }

    fun filterForNewIngredientSpecs(index: Int, newIngredientSpec: IngredientSpec<out Ingredient>) {
        val newIngredientsList = newIngredientSpec.createIngredients(fleet, market)
        pickedIngredientsBySpec[index].removeAll { existingIngredient ->
            newIngredientsList.none { existingIngredient == it }
                    || !newIngredientSpec.canUseIngredientForStack(existingIngredient)
                    || !hasIngredientForSpec(index, newIngredientSpec, existingIngredient)
        }
    }

    fun hasIngredientForSpec(
        index: Int,
        ingredientSpec: IngredientSpec<out Ingredient>,
        ingredient: Ingredient
    ): Boolean {
        var totalAmountRequired = ingredientSpec.getRequired()
        for (i in 0 until pickedIngredientsBySpec.size) {
            if (index == i) continue
            if (pickedIngredientsBySpec[i].any { it == ingredient }) {
                val otherSpec = specs[i]
                totalAmountRequired += otherSpec.getRequired()
            }
        }
        return ingredient.getQuantity() >= totalAmountRequired
    }
}