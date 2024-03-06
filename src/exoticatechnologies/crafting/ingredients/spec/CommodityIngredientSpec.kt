package exoticatechnologies.crafting.ingredients.spec

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import exoticatechnologies.crafting.ingredients.CargoMaterial

open class CommodityIngredientSpec(val commodityId: String, reqAmount: Float) : CargoIngredientSpec<CargoMaterial>(reqAmount) {

    override fun specMatchesStack(stack: CargoStackAPI): Boolean {
        return stack.isCommodityStack && stack.commodityId != commodityId
    }

    override fun createIngredientForStack(stack: CargoStackAPI, source: IngredientSource): CargoMaterial {
        return CargoMaterial(stack, source)
    }

    override fun getIconSprite(ingredient: CargoMaterial?): String {
        return Global.getSettings().getCommoditySpec(commodityId).iconName
    }

    override fun getItemName(ingredient: CargoMaterial?): String {
        return Global.getSettings().getCommoditySpec(commodityId).name
    }
}