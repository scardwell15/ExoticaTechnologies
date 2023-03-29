package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin.SpecialItemRendererAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ModSpecialItemPlugin
import exoticatechnologies.modifications.exotics.impl.HackedMissileForge
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.StringUtils
import kotlin.Int
import kotlin.String

open class ExoticSpecialItemPlugin : ModSpecialItemPlugin() {
    var exotic: Exotic? = null

    var exoticData: ExoticData? = null
        get() {
            if (field == null) {
                if (exotic != null) {
                    field = ExoticData(exotic!!)
                    exotic = null
                } else if (stack != null) {
                    stack.cargo.removeStack(stack)
                    field = ExoticData(ExoticsHandler.EXOTIC_LIST[0].key)
                }
            }
            return field
        }

    override val type: ModType
        get() = ModType.EXOTIC

    override val sprite: SpriteAPI
        get() = Global.getSettings().getSprite("exotics", exoticData!!.key)

    override fun createTooltip(
        tooltip: TooltipMakerAPI,
        expanded: Boolean,
        transferHandler: CargoTransferHandlerAPI,
        stackSource: Any,
        useGray: Boolean
    ) {
        super.createTooltip(tooltip, expanded, transferHandler, stackSource, useGray)

        exoticData?.type?.getItemDescTranslation()?.let {
            StringUtils.getTranslation("ExoticTypes", "ItemTypeText")
                .format("typeName", exoticData!!.type.name)
                .format("typeDescription", it.toStringNoFormats())
                .addToTooltip(tooltip)
        }
    }

    override fun handleParam(index: Int, param: String, stack: CargoStackAPI) {
        when (Param[index]) {
            Param.EXOTIC_ID -> {
                modId = param
                exoticData = ExoticData(modId!!)
                if (exoticData!!.key != modId) {
                    modId = exoticData!!.key
                    stack.specialDataIfSpecial.data.replace(param, modId!!)
                }
            }

            Param.EXOTIC_TYPE -> {
                if (param == "true" || param == "false") {
                    val split = stack.specialDataIfSpecial.data
                        .split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val newData = String.format("%s,NORMAL,%s", split[0], split[1])
                    stack.specialDataIfSpecial.data = newData //fix saves
                    ignoreCrate = param.toBoolean()

                    exoticData!!.type = ExoticType.NORMAL
                } else {
                    exoticData!!.type = ExoticType.valueOf(param)
                }
            }

            Param.IGNORE_CRATE -> ignoreCrate = param.toBoolean()
        }
    }

    private enum class Param {
        EXOTIC_ID, EXOTIC_TYPE, IGNORE_CRATE;

        companion object {
            operator fun get(index: Int): Param {
                return values()[index]
            }
        }
    }
}