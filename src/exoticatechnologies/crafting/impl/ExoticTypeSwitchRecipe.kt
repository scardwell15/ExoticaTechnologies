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
import exoticatechnologies.crafting.ingredients.spec.ExoticChipIngredientSpec
import exoticatechnologies.crafting.ingredients.spec.IngredientSpec
import exoticatechnologies.crafting.ingredients.spec.UpgradeChipIngredientSpec
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui2.impl.crafting.RecipeOutputPreviewPanel
import exoticatechnologies.ui2.impl.crafting.RecipeOutputPreviewPanelContext
import exoticatechnologies.ui2.impl.crafting.RecipeResultContext
import exoticatechnologies.ui2.impl.crafting.RecipeResultPanel
import exoticatechnologies.ui2.impl.scanner.ExoticTooltip
import exoticatechnologies.ui2.util.UIUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.lazywizard.lazylib.ui.LazyFont
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class ExoticTypeSwitchRecipe : ItemRecipe() {
    override fun getName(): String {
        return StringUtils.getString("ExoticTypeSwitchRecipe", "Name")
    }

    override fun getHints(): List<String> {
        return listOf("exotics")
    }

    override fun getDescription(): String {
        return StringUtils.getString("ExoticTypeSwitchRecipe", "Description")
    }

    override fun postCraft(
        recipeOutputPreviewPanelContext: RecipeOutputPreviewPanelContext,
        recipeOutputPreviewPanel: RecipeOutputPreviewPanel,
        craftObject: Any?
    ) {
        recipeOutputPreviewPanel.getPanel()?.let { outputPreviewElement ->
            val recipeResultContext =
                RecipeResultContext(recipeOutputPreviewPanelContext.recipePanelContext.recipe, craftObject)
            val recipeResultPanel = TransferResultPanel(2f, recipeResultContext).apply {
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
        craftingOutputData as TransferTypeResult?
        craftingOutputData?.let {
            val plugin = craftingOutputData.stack.plugin as ExoticSpecialItemPlugin
            recipeResultPanel.apply {
                bgColor = (plugin.exoticData?.getColor() ?: Color.BLACK).setAlpha(40)
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
        return true
    }

    override fun getIngredientSpecs(pickedIngredients: List<List<Ingredient>>?): List<IngredientSpec<out Ingredient>> {
        if (pickedIngredients?.isNotEmpty() == true) {
            pickedIngredients[0] //i literally only care about the first index here
                .mapNotNull { it as? CargoMaterial }
                .firstOrNull { it.stack.isSpecialStack && it.stack.specialDataIfSpecial.id == Exotic.ITEM }
                ?.let {
                    val exoticType = (it.stack.plugin as ExoticSpecialItemPlugin).exoticData?.type
                    if (exoticType != null) {
                        return mutableListOf(
                            ExoticChipIngredientSpec("/.*,(${ExoticType.types.minus(ExoticType.NORMAL.nameKey).keys.joinToString("|")})", 1f),
                            ExoticChipIngredientSpec("/.*,(${ExoticType.types.minus(exoticType.nameKey).keys.joinToString("|")})", 1f)
                        )
                    }
                }
        }
        return mutableListOf(ExoticChipIngredientSpec("/.*,(${ExoticType.types.minus(ExoticType.NORMAL.nameKey).keys.joinToString("|")})", 1f), ExoticChipIngredientSpec("-", 1f))
    }

    override fun getOutputItemStack(ingredients: List<List<Ingredient>>): CargoStackAPI? {
        val firstIngredient = ingredients[0]
            .mapNotNull { it as? CargoMaterial }
            .firstOrNull { it.stack.isSpecialStack && it.stack.specialDataIfSpecial.id == Exotic.ITEM }

        val secondIngredient = ingredients[1]
            .mapNotNull { it as? CargoMaterial }
            .firstOrNull { it.stack.isSpecialStack && it.stack.specialDataIfSpecial.id == Exotic.ITEM }

        if (firstIngredient != null && secondIngredient != null) {
            val firstPlugin = firstIngredient.stack.plugin as ExoticSpecialItemPlugin
            val firstExoticType = firstPlugin.exoticData?.type

            val secondPlugin = secondIngredient.stack.plugin as ExoticSpecialItemPlugin
            val secondExotic = secondPlugin.exoticData?.exotic

            if (firstExoticType != null && secondExotic != null) {
                val newStack = Global.getFactory().createCargoStack(
                    CargoAPI.CargoItemType.SPECIAL,
                    secondExotic.getNewSpecialItemData(firstExoticType),
                    null
                )
                newStack.size = 1f
                return newStack
            }
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

        return TransferTypeResult(craftedOutput)
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
            tooltip.addPara(StringUtils.getString("ExoticTypeSwitchRecipe", "OutputName"), 0f).position.rightOfTop(iconElement, 4f)
            val nameElement = tooltip.prev
            tooltip.setParaFontDefault()
            val gigaLabel = tooltip.addPara(StringUtils.getString("ExoticTypeSwitchRecipe", "OutputDesc1"), 0f)
            gigaLabel.position.belowLeft(nameElement, 2f)
            val labelElement = tooltip.prev
            val gigaLabel2 = tooltip.addPara(StringUtils.getString("ExoticTypeSwitchRecipe", "OutputDesc2"), 0f)
            gigaLabel2.position.belowLeft(labelElement, 2f)
        } else if (pickedIngredients[1].isEmpty()) {
            val plugin = (pickedIngredients[0].first() as CargoMaterial).stack.plugin as ExoticSpecialItemPlugin
            val exoticData = plugin.exoticData!!

            tooltip.addImage(Global.getSettings().getSpriteName("ui", "32x_crossed_circle2"), 64f, 0f)
            val iconElement = tooltip.prev
            iconElement.position.inTL(4f, 16f)
            UIUtils.addSpriteOverlayOver(tooltip, iconElement, exoticData.type.sprite!!, exoticData.type.colorOverlay)

            tooltip.setParaFontVictor14()

            val exoticName = StringUtils.getTranslation("ExoticTypeSwitchRecipe", "OutputNameWithType")
                .format("exoticType", exoticData.type.name)
                .format("exoticName", StringUtils.getString("ExoticTypeSwitchRecipe", "OutputName"))
                .toStringNoFormats()

            tooltip.addPara(exoticName, exoticData.type.colorOverlay,0f).position.rightOfTop(iconElement, 4f)
            val nameElement = tooltip.prev
            tooltip.setParaFontDefault()
            val gigaLabel = tooltip.addPara(StringUtils.getString("ExoticTypeSwitchRecipe", "OutputDesc1"), 0f)
            gigaLabel.position.belowLeft(nameElement, 2f)
            val labelElement = tooltip.prev
            val gigaLabel2 = tooltip.addPara(StringUtils.getString("ExoticTypeSwitchRecipe", "OutputDesc2"), 0f)
            gigaLabel2.position.belowLeft(labelElement, 2f)
        } else {
            val firstIngredient = pickedIngredients[0].first() as CargoMaterial
            val firstPlugin = firstIngredient.stack.plugin as ExoticSpecialItemPlugin
            val firstExoticType = firstPlugin.exoticData?.type

            val secondIngredient = pickedIngredients[1].first() as CargoMaterial
            val secondPlugin = secondIngredient.stack.plugin as ExoticSpecialItemPlugin
            val secondExotic = secondPlugin.exoticData?.exotic

            if (firstExoticType != null && secondExotic != null) {
                val exoticData = ExoticData(secondExotic, firstExoticType)

                tooltip.addImage(secondExotic.iconPath, 64f, 0f)
                val iconElement = tooltip.prev
                iconElement.position.inTL(4f, 16f)
                UIUtils.addSpriteOverlayOver(tooltip, iconElement, firstExoticType.sprite!!, firstExoticType.colorOverlay)

                tooltip.setParaFontVictor14()
                tooltip.addPara(
                    exoticData.getNameTranslation().toStringNoFormats(),
                    exoticData.getColor(),0f).position.rightOfTop(iconElement, 4f)
                val nameElement = tooltip.prev
                tooltip.setParaFontDefault()
                val gigaLabel = tooltip.addPara(StringUtils.getString("ExoticTypeSwitchRecipe", "OutputDesc1"), 0f)
                gigaLabel.position.belowLeft(nameElement, 2f)
                val labelElement = tooltip.prev
                val gigaLabel2 = tooltip.addPara(StringUtils.getString("ExoticTypeSwitchRecipe", "OutputDesc2"), 0f)
                gigaLabel2.position.belowLeft(labelElement, 2f)
            }
        }
    }
}

class TransferResultPanel(endLife: Float, currContext: RecipeResultContext) : RecipeResultPanel(endLife, currContext) {
    override fun render(alphaMult: Float) {
        val craftingOutputData: Any? = currContext.craftingOutputData
        craftingOutputData as TransferTypeResult?
        craftingOutputData?.let {
            val plugin = it.stack.plugin as ExoticSpecialItemPlugin
            val exoticData = plugin.exoticData

            val sprite = Global.getSettings().getSprite(exoticData?.exotic?.iconPath)
            sprite.alphaMult = alphaMult * currAlpha
            sprite.setNormalBlend()
            sprite.renderAtCenter(pos.x + 4f + sprite.width / 2f, pos.centerY)

            val typeSprite = Global.getSettings().getSprite(exoticData?.type?.sprite)
            typeSprite.alphaMult = alphaMult * currAlpha
            typeSprite.setNormalBlend()
            typeSprite.color = exoticData?.type?.colorOverlay
            typeSprite.renderAtCenter(pos.x + 4f + sprite.width / 2f, pos.centerY)

            titleDrawableString.text = exoticData?.getNameTranslation()?.toStringNoFormats() ?: "what"
            titleDrawableString.anchor = LazyFont.TextAnchor.TOP_LEFT
            titleDrawableString.baseColor = Color.BLACK.setAlpha((255 * alphaMult * currAlpha).toInt())
            titleDrawableString.draw(pos.x + 8f + sprite.width, pos.centerY + titleDrawableString.height)
            titleDrawableString.baseColor = (exoticData?.getColor() ?: Color.BLACK).setAlpha((255 * alphaMult * currAlpha).toInt())
            titleDrawableString.draw(pos.x + 8f + sprite.width - 1f, pos.centerY + titleDrawableString.height - 1f)

            subtextDrawableString.text = exoticData?.type?.name ?: "what2"
            subtextDrawableString.anchor = LazyFont.TextAnchor.TOP_LEFT
            subtextDrawableString.baseColor = Color.BLACK.setAlpha((255 * alphaMult * currAlpha).toInt())
            subtextDrawableString.draw(pos.x + 8f + sprite.width, pos.centerY)
            subtextDrawableString.baseColor = (exoticData?.type?.colorOverlay ?: Color.BLACK).setAlpha((255 * alphaMult * currAlpha).toInt())
            subtextDrawableString.draw(pos.x + 8f + sprite.width - 1f, pos.centerY - 1f)
        }
    }

    companion object {
        val titleDrawableString = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt").createText()
        val subtextDrawableString = LazyFont.loadFont(Global.getSettings().getString("defaultFont")).createText()
    }
}

class TransferTypeResult(val stack: CargoStackAPI)