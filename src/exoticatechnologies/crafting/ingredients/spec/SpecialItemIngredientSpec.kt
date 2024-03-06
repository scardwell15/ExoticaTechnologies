package exoticatechnologies.crafting.ingredients.spec

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import exoticatechnologies.crafting.ingredients.CargoMaterial

open class SpecialItemIngredientSpec(val specialId: String, val specialData: String?, reqAmount: Float) :
    CargoIngredientSpec<CargoMaterial>(reqAmount) {
    val idRegex: Regex
    val dataRegex: Regex?

    init {
        if (specialId.startsWith("/")) {
            idRegex = Regex(specialId.substring(1))
        } else {
            idRegex = Regex(Regex.escape(specialId))
        }

        if (specialData != null) {
            if (specialData.startsWith("/") == true) {
                dataRegex = Regex(specialData.substring(1))
            } else {
                dataRegex = Regex(Regex.escape(specialData))
            }
        } else {
            dataRegex = null
        }
    }

    override fun specMatchesStack(stack: CargoStackAPI): Boolean {
        return stack.isSpecialStack && stack.specialDataIfSpecial != null
                && idRegex.matches(stack.specialDataIfSpecial.id)
                && (dataRegex == null || (stack.specialDataIfSpecial.data != null && dataRegex.matches(stack.specialDataIfSpecial.data)))
    }

    override fun createIngredientForStack(stack: CargoStackAPI, source: IngredientSource): CargoMaterial {
        return CargoMaterial(stack, source)
    }

    override fun getIconSprite(ingredient: CargoMaterial?): String {
        return Global.getSettings().getSpecialItemSpec(specialId).iconName
    }

    override fun getItemName(ingredient: CargoMaterial?): String {
        return ingredient?.stack?.plugin?.name ?: Global.getSettings().getSpecialItemSpec(specialId).name
    }
}