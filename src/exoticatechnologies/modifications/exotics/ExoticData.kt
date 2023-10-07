package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.modifications.exotics.types.ExoticTypePanelPlugin
import exoticatechnologies.ui.SpritePanelPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.StringUtils.Translation
import org.json.JSONException
import org.json.JSONObject
import java.awt.Color

class ExoticData(key: String, var type: ExoticType = ExoticType.NORMAL) {
    var key = checkUpdateExotic(key)
        get() {
            if (checkUpdateExotic(field) != field) {
                field = checkUpdateExotic(field)
            }
            return field
        }
        set(value) {
            if (checkUpdateExotic(value) != value) {
                field = checkUpdateExotic(value)
            } else {
                field = value
            }
        }

    constructor(key:String) : this(key, ExoticType.NORMAL)
    constructor(exotic:Exotic, type: ExoticType) : this(exotic.key, type)
    constructor(exotic:Exotic) : this(exotic, ExoticType.NORMAL)
    @Throws(JSONException::class)
    constructor(obj: JSONObject) : this(obj.getString("key"), ExoticType.valueOf(obj.getString("type")))

    val exotic: Exotic
        get () {
            if (checkUpdateExotic(key) != key) {
                key = checkUpdateExotic(key)
            }
            return ExoticsHandler.EXOTICS[key]!!
        }

    fun getNameTranslation(): Translation {
        return StringUtils.getTranslation("ExoticTypes", type.nameKey)
            .format("exoticName", exotic.name)
    }

    fun addExoticIcon(tooltip: TooltipMakerAPI): Pair<SpritePanelPlugin, SpritePanelPlugin?> {
        val sprite = Global.getSettings().getSprite("exotics", exotic.icon)
        val spritePlugin = SpritePanelPlugin(sprite)
        val iconPanel = Global.getSettings().createCustom(64f, 64f, spritePlugin)
        spritePlugin.panel = iconPanel
        tooltip.addCustom(iconPanel, 0f)

        val typeOverlay: SpritePanelPlugin? = addExoticOverlayOver(tooltip, iconPanel)

        return spritePlugin to typeOverlay
    }

    fun addExoticOverlayOver(tooltip: TooltipMakerAPI, exoticIcon: UIComponentAPI): SpritePanelPlugin? {
        var typeOverlay: SpritePanelPlugin? = null

        if (type.sprite != null) {
            typeOverlay = ExoticTypePanelPlugin(type)
            val overlayPanel = Global.getSettings().createCustom(exoticIcon.position.width, exoticIcon.position.height, typeOverlay)
            tooltip.addCustom(overlayPanel, -exoticIcon.position.height)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExoticData

        if (type != other.type) return false
        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }

    companion object {
        val updateMap = mutableMapOf("HangarForge" to "PhasedFighterTether",
            "HangarForgeMissiles" to "HackedMissileForge")

        fun checkUpdateExotic(exoticKey: String): String {
            return updateMap[exoticKey] ?: exoticKey
        }
    }
}

