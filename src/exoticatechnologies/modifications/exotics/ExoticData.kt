package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.modifications.exotics.types.ExoticTypePanelPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.StringUtils.Translation
import org.json.JSONException
import org.json.JSONObject
import java.awt.Color

class ExoticData(var key: String, val type: ExoticType = ExoticType.NORMAL) {
    constructor(key:String) : this(key, ExoticType.NORMAL)
    constructor(exotic:Exotic, type: ExoticType) : this(exotic.key, type)
    constructor(exotic:Exotic) : this(exotic, ExoticType.NORMAL)

    @Throws(JSONException::class)
    constructor(obj: JSONObject) : this(obj.getString("key"), ExoticType.valueOf(obj.getString("type")))

    val exotic: Exotic
        get () = ExoticsHandler.EXOTICS[key]!!

    fun getNameTranslation(): Translation {
        return StringUtils.getTranslation("ExoticTypes", type.nameKey)
            .format("exoticName", exotic.name)
    }

    fun addExoticIcon(tooltip: TooltipMakerAPI): Pair<UIComponentAPI, UIComponentAPI?> {
        tooltip.addImage(exotic.icon, 64f, 0f)
        val exoticIcon: UIComponentAPI = tooltip.prev
        val typeOverlay: UIComponentAPI? = addExoticOverlayOver(tooltip, exoticIcon)

        return exoticIcon to typeOverlay
    }

    fun addExoticOverlayOver(tooltip: TooltipMakerAPI, exoticIcon: UIComponentAPI): UIComponentAPI? {
        var typeOverlay: UIComponentAPI? = null

        if (type.sprite != null) {
            val overlayPanel = Global.getSettings().createCustom(exoticIcon.position.width, exoticIcon.position.height, ExoticTypePanelPlugin(type))
            tooltip.addCustom(overlayPanel, -exoticIcon.position.height)
            typeOverlay = tooltip.prev
        }

        return typeOverlay
    }

    fun mutateGenerationContext(context: ShipModFactory.GenerationContext) {
        type.mutateGenerationContext(context)
    }

    fun getColor(): Color {
        return type.getMergedColor(exotic.color)
    }

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("key", exotic.key)
        obj.put("type", type.nameKey)
        return obj
    }
}

