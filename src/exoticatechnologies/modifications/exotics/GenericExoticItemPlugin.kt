package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemPlugin.SpecialItemRendererAPI
import exoticatechnologies.config.FactionConfig
import exoticatechnologies.config.FactionConfigLoader
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import lombok.extern.log4j.Log4j
import org.json.JSONObject
import org.magiclib.kotlin.setAlpha
import java.util.*

@Log4j
class GenericExoticItemPlugin : ExoticSpecialItemPlugin() {
    override fun getName(): String {
        return String.format("%s - %s", super.getName(), exoticData!!.getNameTranslation().toStringNoFormats())
    }

    override fun resolveDropParamsToSpecificItemData(params: String, random: Random): String? {
        var exotic: Exotic? = null
        var type: ExoticType = ExoticType.NORMAL

        val paramsObj = JSONObject(params)
        if (paramsObj.optBoolean("rng")) {
            exotic = ExoticsGenerator.getDefaultExoticPicker(random).pick()

            if (random.nextFloat() < 0.15f) {
                type = ExoticsGenerator.getDefaultTypePicker(random, exotic).pick()
            }
        } else if (paramsObj.optString("faction") != null) {
            val factionConfig = FactionConfigLoader.getFactionConfig(paramsObj.getString("faction"))
            exotic = ExoticsGenerator.getExoticPicker(random, factionConfig.allowedExotics).pick()

            if (random.nextFloat() < 0.15f) {
                type = ExoticsGenerator.getTypePicker(random, exotic, factionConfig.allowedExoticTypes).pick()
            }
        } else if (paramsObj.optString("exotic") != null) {
            exotic = ExoticsHandler.EXOTICS[paramsObj.getString("exotic")]
            var typeParam: String? = paramsObj.optString("type")

            if (exotic != null) {
                if (typeParam == null) {
                    if (random.nextFloat() < 0.15f) {
                        type = ExoticsGenerator.getDefaultTypePicker(random, exotic).pick()
                    }
                } else {
                    type = ExoticType.valueOf(typeParam)
                }
            }
        }

        exotic ?: return null
        return "${exotic.key},${type.nameKey}"
    }

    override fun render(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        alphaMult: Float,
        glowMult: Float,
        renderer: SpecialItemRendererAPI
    ) {
        val exoticSprite = Global.getSettings().getSprite("exotics", exoticData!!.exotic.icon)
        val tX = 0.4f
        val tY = 0.37f
        val tW = 0.70f
        val tH = 0.70f
        val mult = 1f
        exoticSprite.alphaMult = alphaMult * mult
        exoticSprite.setNormalBlend()
        exoticSprite.setSize(tW * exoticSprite.width, tH * exoticSprite.height)
        exoticSprite.renderAtCenter(x + (1 + tX) * (w * tW) / 2, y + (1 + tY) * (h * tH) / 2)

        exoticData!!.type.sprite?.let {
            val overlay = Global.getSettings().getSprite(it)
            overlay.alphaMult = alphaMult * mult
            overlay.setNormalBlend()
            overlay.setSize(tW * overlay.width, tH * overlay.height)
            overlay.color = exoticData!!.type.colorOverlay.setAlpha(255)
            overlay.renderAtCenter(x + (1 + tX) * (w * tW) / 2, y + (1 + tY) * (h * tH) / 2)
        }

        val cx = x + w / 2f
        val cy = y + h / 2f
        val blX = cx - 24f
        val blY = cy - 17f
        val tlX = cx - 14f
        val tlY = cy + 26f
        val trX = cx + 28f
        val trY = cy + 25f
        val brX = cx + 20f
        val brY = cy - 18f

        renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult * 0.75f, false)
    }
}