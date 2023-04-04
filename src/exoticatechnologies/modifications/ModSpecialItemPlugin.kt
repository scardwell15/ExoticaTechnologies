package exoticatechnologies.modifications

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin.SpecialItemRendererAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.upgrades.UpgradesHandler

abstract class ModSpecialItemPlugin : BaseSpecialItemPlugin() {
    open var modId: String? = null

    open var ignoreCrate = false
    abstract val type: ModType
    val mod: Modification?
        get() = if (ModType.UPGRADE == type) {
            UpgradesHandler.UPGRADES[modId]
        } else {
            ExoticsHandler.EXOTICS[modId]
        }
    abstract val sprite: SpriteAPI

    @Transient
    var renderer: SpecialItemRendererAPI? = null

    override fun init(stack: CargoStackAPI) {
        super.init(stack)
        var passedParams = stack.specialDataIfSpecial.data
        if (passedParams == null) {
            passedParams = spec.params
        }

        if (passedParams.isNotEmpty()) {
            passedParams.split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
                .forEachIndexed { i, param ->
                    handleParam(i, param.trim { it <= ' ' }, stack)
                }
        }
    }

    open var stack: CargoStackAPI?
        get() = stack
        set(stack) {
            super.stack = stack
        }

    override fun createTooltip(
        tooltip: TooltipMakerAPI,
        expanded: Boolean,
        transferHandler: CargoTransferHandlerAPI,
        stackSource: Any,
        useGray: Boolean
    ) {
        val opad = 10.0f
        tooltip.addTitle(this.name)
        val design = this.designType
        Misc.addDesignTypePara(tooltip, design, opad)
        if (!spec.desc.isEmpty()) {
            var c = Misc.getTextColor()
            if (useGray) {
                c = Misc.getGrayColor()
            }
            tooltip.addPara(spec.desc, c, opad)
        }
        if (mod != null) {
            tooltip.addPara(mod!!.description, Misc.getTextColor(), opad)
        }
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
        this.renderer = renderer

        val upgradeSprite = sprite
        val tX = 0.43f
        val tY = 0.38f
        val tW = 0.70f
        val tH = 0.70f
        val mult = 1f
        upgradeSprite.alphaMult = alphaMult * mult
        upgradeSprite.setNormalBlend()
        upgradeSprite.setSize(tW * upgradeSprite.width, tH * upgradeSprite.height)
        upgradeSprite.renderAtCenter(x + (1 + tX) * (w * tW) / 2, y + (1 + tY) * (h * tH) / 2)

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

    fun render(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        alphaMult: Float,
        glowMult: Float
    ) {
        renderer?.let {
            return render(x, y, w, h, alphaMult, glowMult, it)
        }
    }
    protected abstract fun handleParam(index: Int, param: String, stack: CargoStackAPI)
    override fun shouldRemoveOnRightClickAction(): Boolean {
        return false
    }

    enum class ModType {
        UPGRADE, EXOTIC
    }
}