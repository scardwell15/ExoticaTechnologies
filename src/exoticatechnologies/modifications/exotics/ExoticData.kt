package exoticatechnologies.modifications.exotics

import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.StringUtils.Translation

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
}

enum class ExoticType(val nameKey: String, val positiveMult: Float = 1f, val negativeMult: Float = 1f) {
    NORMAL("NORMAL"),
    CORRUPTED("CORRUPTED", positiveMult = 1.5f, negativeMult = 1.5f)
}