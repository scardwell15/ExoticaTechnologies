package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.StringUtils.Translation
import java.awt.Color

class ExoticData(val key: String, val type: ExoticType = ExoticType.NORMAL) {
    constructor(key:String) : this(key, ExoticType.NORMAL)
    constructor(exotic:Exotic, type: ExoticType) : this(exotic.key, type)
    constructor(exotic:Exotic) : this(exotic, ExoticType.NORMAL)

    val exotic: Exotic
        get () = ExoticsHandler.EXOTICS[key]!!

    fun getNameTranslation(): Translation {
        return StringUtils.getTranslation("ExoticTypes", type.nameKey)
            .format("exoticName", exotic.name)
    }

    fun addExoticIcon(tooltip: TooltipMakerAPI): Pair<UIComponentAPI, UIComponentAPI?> {
        tooltip.addImage(exotic.icon, 64f, 0f)
        val exoticIcon: UIComponentAPI = tooltip.prev
        var typeOverlay: UIComponentAPI? = null

        if (type.sprite != null) {
            tooltip.addImage(type.sprite, 64f, -32f)
            typeOverlay = tooltip.prev
        }

        return exoticIcon to typeOverlay
    }

    fun addExoticOverlayOverPrev(tooltip: TooltipMakerAPI): Pair<UIComponentAPI, UIComponentAPI?> {
        val exoticIcon: UIComponentAPI = tooltip.prev
        var typeOverlay: UIComponentAPI? = null

        if (type.sprite != null) {
            tooltip.addImage(type.sprite, 64f, -32f)
            typeOverlay = tooltip.prev
        }

        return exoticIcon to typeOverlay
    }

    fun getColor(): Color {
        return type.getMergedColor(exotic.color)
    }
}

enum class ExoticType(val nameKey: String, val positiveMult: Float = 1f, val negativeMult: Float = 1f, val colorOverlay: Color = Color(255, 255, 255), val sprite: String? = null) {
    NORMAL("NORMAL"),
    CORRUPTED("CORRUPTED", positiveMult = 1.5f, negativeMult = 1.5f, colorOverlay = Color(255, 0, 0, 125), sprite = "graphics/icons/overlays/corrupted.png");

    fun getName(): String {
        return StringUtils.getTranslation("ExoticTypes", nameKey)
            .format("exoticName", "")
            .toStringNoFormats()
    }

    fun getMergedColor(otherColor: Color): Color {
        val ratio = colorOverlay.alpha / 255
        val red = (otherColor.red - (255 - colorOverlay.red) * ratio).coerceIn(0, 255)
        val green = (otherColor.green - (255 - colorOverlay.green) * ratio).coerceIn(0, 255)
        val blue = (otherColor.blue - (255 - colorOverlay.blue) * ratio).coerceIn(0, 255)

        return Color(red, green, blue, otherColor.alpha)
    }
}