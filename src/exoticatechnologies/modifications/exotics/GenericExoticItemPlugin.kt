package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemPlugin.SpecialItemRendererAPI
import lombok.extern.log4j.Log4j
import org.magiclib.kotlin.setAlpha

@Log4j
class GenericExoticItemPlugin : ExoticSpecialItemPlugin() {
    override fun getName(): String {
        return String.format("%s - %s", super.getName(), exoticData!!.getNameTranslation().toStringNoFormats())
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
        val exoticSprite = Global.getSettings().getSprite("exotics", exoticData!!.key)
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
            overlay.setSize(tW * exoticSprite.width, tH * exoticSprite.height)
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

        renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult * 0.5f, false)
    }
}