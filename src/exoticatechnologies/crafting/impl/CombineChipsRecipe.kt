package exoticatechnologies.crafting.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.crafting.ItemRecipe
import exoticatechnologies.crafting.ingredients.CargoMaterial
import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.crafting.ingredients.spec.IngredientSpec
import exoticatechnologies.crafting.ingredients.spec.UpgradeChipIngredientSpec
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui2.impl.crafting.RecipeOutputPreviewPanel
import exoticatechnologies.ui2.impl.crafting.RecipeOutputPreviewPanelContext
import exoticatechnologies.ui2.impl.crafting.RecipeResultContext
import exoticatechnologies.ui2.impl.crafting.RecipeResultPanel
import exoticatechnologies.util.Utilities
import org.lazywizard.lazylib.ui.LazyFont
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class CombineChipsRecipe : ItemRecipe() {
    override fun getName(): String {
        return "Combine upgrade chips"
    }

    override fun getHints(): List<String> {
        return listOf("upgrades")
    }

    override fun getDescription(): String {
        return "Combine two chips with the same upgrade and level to make one with a higher level."
    }

    override fun postCraft(
        recipeOutputPreviewPanelContext: RecipeOutputPreviewPanelContext,
        recipeOutputPreviewPanel: RecipeOutputPreviewPanel,
        craftObject: Any?
    ) {
        recipeOutputPreviewPanel.getPanel()?.let { outputPreviewElement ->
            val recipeResultContext = RecipeResultContext(recipeOutputPreviewPanelContext.recipePanelContext.recipe, craftObject)
            val recipeResultPanel = CombineResultPanel(2f, recipeResultContext).apply {
                panelWidth = recipeOutputPreviewPanel.innerWidth
                panelHeight = 80f
                beginFade = 0.66f
            }

            recipeResultPanel.layoutPanel(outputPreviewElement, null).position.inBMid(100f)
        }
    }

    override fun modifyPostCraftingOutputTooltip(
        recipeResultPanel: RecipeResultPanel,
        tooltip: TooltipMakerAPI,
        panelWidth: Float,
        panelHeight: Float,
        craftingOutputData: Any?
    ) {
        craftingOutputData as CombineChipResult?
        craftingOutputData?.let {
            val plugin = craftingOutputData.stack.plugin as UpgradeSpecialItemPlugin
            recipeResultPanel.apply {
                bgColor = (plugin.upgrade?.color ?: Color.BLACK).setAlpha(40)
                renderBackground = true
                outlineColor = Color.YELLOW.setAlpha(254)
                renderBorder = true
            }
        }
    }

    override fun canUseIngredient(
        index: Int,
        ingredientSpec: IngredientSpec<out Ingredient>,
        ingredient: Ingredient
    ): Boolean {
        ingredient as CargoMaterial
        val plugin = ingredient.stack.plugin as UpgradeSpecialItemPlugin
        val maxLevel = plugin.upgrade?.maxLevel ?: 0
        if (plugin.upgradeLevel >= maxLevel)
            return false
        return true
    }

    override fun getIngredientSpecs(pickedIngredients: List<List<Ingredient>>?): List<IngredientSpec<out Ingredient>> {
        if (pickedIngredients?.isNotEmpty() == true) {
            pickedIngredients[0] //i literally only care about the first index here
                .mapNotNull { it as? CargoMaterial }
                .firstOrNull { it.stack.isSpecialStack && it.stack.specialDataIfSpecial.id == Upgrade.ITEM }
                ?.let {
                    return mutableListOf(
                        UpgradeChipIngredientSpec("/.*", 1f),
                        UpgradeChipIngredientSpec(it.stack.specialDataIfSpecial.data, 1f)
                    )
                }
        }
        return mutableListOf(UpgradeChipIngredientSpec("/.*", 1f), UpgradeChipIngredientSpec("/.*", 1f))
    }

    override fun getOutputItemStack(ingredients: List<List<Ingredient>>): CargoStackAPI? {
        ingredients
            .flatten()
            .mapNotNull { it as? CargoMaterial }
            .firstOrNull { it.stack.isSpecialStack && it.stack.specialDataIfSpecial.id == Upgrade.ITEM }
            ?.let {
                val plugin = it.stack.plugin as UpgradeSpecialItemPlugin
                val maxLevel = plugin.upgrade?.maxLevel ?: 1
                var levelIncrease = 1
                if (ShipModFactory.random.nextFloat() <= 0.15f) {
                    levelIncrease = 3
                } else if (ShipModFactory.random.nextFloat() <= 0.3f) {
                    levelIncrease = 2
                }
                levelIncrease = levelIncrease.coerceAtMost(maxLevel - plugin.upgradeLevel)

                val newStack = Global.getFactory().createCargoStack(
                    CargoAPI.CargoItemType.SPECIAL,
                    plugin.upgrade?.getNewSpecialItemData(plugin.upgradeLevel + levelIncrease),
                    null
                )
                newStack.size = 1f
                return newStack
            }

        return null
    }

    override fun crafted(
        fleet: CampaignFleetAPI,
        market: MarketAPI?,
        ingredients: List<List<Ingredient>>,
        recipeOutputPreviewPanelContext: RecipeOutputPreviewPanelContext?
    ): Any? {
        val craftedOutput = super.crafted(fleet, market, ingredients, recipeOutputPreviewPanelContext)
        Utilities.mergeChipsIntoCrate(fleet.cargo)
        if (craftedOutput !is CargoStackAPI) return null

        ingredients
            .flatten()
            .mapNotNull { it as? CargoMaterial }
            .firstOrNull { it.stack.isSpecialStack && it.stack.specialDataIfSpecial.id == Upgrade.ITEM }
            ?.let {
                val ingredientPlugin = it.stack.plugin as UpgradeSpecialItemPlugin
                val ingredientLevel = ingredientPlugin.upgradeLevel
                val craftedPlugin = craftedOutput.plugin as UpgradeSpecialItemPlugin
                val craftedLevel = craftedPlugin.upgradeLevel
                val difference = craftedLevel - ingredientLevel

                return CombineChipResult(craftedOutput, difference)
            }

        return null
    }
    override fun modifyOutputPreviewTooltip(
        tooltip: TooltipMakerAPI,
        panelWidth: Float,
        panelHeight: Float,
        pickedIngredients: List<List<Ingredient>>
    ) {
        if (pickedIngredients[0].isEmpty()) {
            tooltip.addImage(Global.getSettings().getSpriteName("ui", "32x_crossed_circle2"), 64f, 0f)
            val iconElement = tooltip.prev
            iconElement.position.inTL(4f, 16f)

            tooltip.setParaFontVictor14()
            tooltip.addPara("Upgrade Chip", 0f).position.rightOfTop(iconElement, 4f)
            val nameElement = tooltip.prev
            tooltip.setParaFontDefault()
            val gigaLabel = tooltip.addPara("Creates an upgrade chip of a higher level.", 0f)
            gigaLabel.position.belowLeft(nameElement, 2f)
            val labelElement = tooltip.prev
            val gigaLabel2 = tooltip.addPara("Lower chance for additional levels.", 0f)
            gigaLabel2.position.belowLeft(labelElement, 2f)
        } else {
            val plugin = (pickedIngredients[0].first() as CargoMaterial).stack.plugin as UpgradeSpecialItemPlugin
            val upgrade = plugin.upgrade!!

            tooltip.addImage(upgrade.iconPath, 64f, 0f)
            val iconElement = tooltip.prev
            iconElement.position.inTL(4f, 16f)

            tooltip.setParaFontVictor14()
            tooltip.addPara("Upgrade Chip - ${upgrade.name}", 0f).position.rightOfTop(iconElement, 4f)
            val nameElement = tooltip.prev
            tooltip.setParaFontDefault()
            val gigaLabel = tooltip.addPara("Creates an upgrade chip of a higher level.", 0f)
            gigaLabel.position.belowLeft(nameElement, 2f)
            val labelElement = tooltip.prev
            val gigaLabel2 = tooltip.addPara("Lower chance for additional levels.", 0f)
            gigaLabel2.position.belowLeft(labelElement, 2f)
        }
    }
}

class CombineResultPanel(endLife: Float, currContext: RecipeResultContext) : RecipeResultPanel(endLife, currContext) {
    override fun render(alphaMult: Float) {
        val craftingOutputData: Any? = currContext.craftingOutputData
        craftingOutputData as CombineChipResult?
        craftingOutputData?.let {
            val plugin = it.stack.plugin as UpgradeSpecialItemPlugin
            val sprite = Global.getSettings().getSprite(plugin.upgrade?.iconPath)
            sprite.alphaMult = alphaMult * currAlpha
            sprite.setNormalBlend()
            sprite.renderAtCenter(pos.x + 4f + sprite.width / 2f, pos.centerY)

            titleDrawableString.text = plugin.upgrade?.name ?: "what"
            titleDrawableString.anchor = LazyFont.TextAnchor.TOP_LEFT
            titleDrawableString.baseColor = Color.BLACK.setAlpha((255 * alphaMult * currAlpha).toInt())
            titleDrawableString.draw(pos.x + 8f + sprite.width, pos.centerY + titleDrawableString.height)
            titleDrawableString.baseColor = Color.WHITE.setAlpha((255 * alphaMult * currAlpha).toInt())
            titleDrawableString.draw(pos.x + 8f + sprite.width - 1f, pos.centerY + titleDrawableString.height - 1f)

            subtextDrawableString.text = "LVL ${plugin.upgradeLevel} (+${craftingOutputData.levelIncrease})"
            subtextDrawableString.anchor = LazyFont.TextAnchor.TOP_LEFT
            subtextDrawableString.baseColor = Color.BLACK.setAlpha((255 * alphaMult * currAlpha).toInt())
            subtextDrawableString.draw(pos.x + 8f + sprite.width, pos.centerY)
            subtextDrawableString.baseColor = Color.WHITE.setAlpha((255 * alphaMult * currAlpha).toInt())
            subtextDrawableString.draw(pos.x + 8f + sprite.width - 1f, pos.centerY - 1f)
        }
    }

    companion object {
        val titleDrawableString = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt").createText()
        val subtextDrawableString = LazyFont.loadFont(Global.getSettings().getString("defaultFont")).createText()
    }
}

class CombineChipResult(val stack: CargoStackAPI, val levelIncrease: Int = 0)