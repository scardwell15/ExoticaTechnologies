package exoticatechnologies.ui

import com.fs.starfarer.api.ui.BaseTooltipCreator
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lombok.RequiredArgsConstructor

@RequiredArgsConstructor
class StringTooltip(private val tooltip: TooltipMakerAPI? = null, private val description: String? = null) : BaseTooltipCreator() {
    override fun getTooltipWidth(tooltipParam: Any): Float {
        return Math.min(tooltip!!.computeStringWidth(description), 300f)
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
        tooltip.addPara(description, 3f)
    }
}