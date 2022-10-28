package exoticatechnologies.ui.impl.shop

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthHandler
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.util.StringUtils
import kotlin.math.max
import kotlin.math.min

class ShipHeaderUIPlugin(
    dialog: InteractionDialogAPI,
    var member: FleetMemberAPI, var mods: ShipModifications, var parentPanel: CustomPanelAPI
) : InteractiveUIPanelPlugin() {
    private val pad = 3f
    private val opad = 10f

    override var panelWidth: Float = Global.getSettings().screenHeight * 0.65f
    override var panelHeight: Float = max(100f, Global.getSettings().screenHeight * 0.166f)

    var market: MarketAPI = dialog.interactionTarget.market
    var lastBandwidth: Float = -1f

    var shipImgTooltip: TooltipMakerAPI? = null

    var shipTextTooltip: TooltipMakerAPI? = null
    var baseBandwidthText: LabelAPI? = null
    var exoticBandwidthText: LabelAPI? = null

    var bandwidthTooltip: TooltipMakerAPI? = null
    var bandwidthUpgradeLabel: LabelAPI? = null
    var bandwidthButton: ButtonAPI? = null

    override fun advancePanel(amount: Float) {
        val newBandwidth = mods.getBandwidthWithExotics(member)
        if (newBandwidth != lastBandwidth) {
            setBandwidthText(newBandwidth)
        }
    }

    fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
        val shipNameColor = member.captain.faction.baseUIColor

        val panel: CustomPanelAPI =
            parentPanel.createCustomPanel(panelWidth, panelHeight, this)

        // Ship image with tooltip of the ship class
        val shipImg = panel.createUIElement(iconSize, iconSize, false)
        shipImg.addShipList(1, 1, iconSize, Misc.getBasePlayerColor(), mutableListOf(member), 0f)
        panel.addUIElement(shipImg).inTL(0f, 0f)

        // Ship name, class, bandwidth
        val shipText = panel.createUIElement(panelWidth - iconSize, 22f * 3f, false)
        shipText.setParaOrbitronLarge()
        shipText.addPara("${member.shipName} (${member.hullSpec.nameWithDesignationWithDashClass})", shipNameColor, 0f)
        shipText.setParaFontDefault()

        baseBandwidthText = shipText.addPara("", 0f)
        setBandwidthText(mods.getBandwidthWithExotics(member))

        panel.addUIElement(shipText).rightOfTop(shipImg, pad)

        val bandwidthHeight = panelHeight - shipText.position.height
        bandwidthTooltip = panel.createUIElement(panelWidth - iconSize, bandwidthHeight, false)

        bandwidthUpgradeLabel = bandwidthTooltip?.addPara("", 0f)
        bandwidthButton = bandwidthTooltip?.addButton(
            StringUtils.getString("BandwidthDialog", "BandwidthPurchase"), "test",
            Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.C2_MENU, 72F, 22F, 3F
        )

        buttons[bandwidthButton!!] = BandwidthButtonHandler(this)

        setBandwidthUpgradeLabel()

        panel.addUIElement(bandwidthTooltip).belowLeft(shipText, pad)

        shipImgTooltip = shipImg
        shipTextTooltip = shipText

        // done, add row to TooltipMakerAPI
        tooltip.addCustom(panel, opad)

        return panel
    }

    private fun setBandwidthText(bandwidth: Float) {
        val baseBandwidth = mods.getBaseBandwidth(member)

        baseBandwidthText?.let {
            modifyBandwidthText(it, baseBandwidth, "CommonOptions", "BaseBandwidthForShip")
        }

        if (bandwidth != baseBandwidth) {
            //something installed that increases bandwidth. bandwidth value is exotic bandwidth
            if (exoticBandwidthText == null) {
                exoticBandwidthText = shipTextTooltip?.addPara("", 3f)

                //reset position of text tooltip because of increased height
                shipTextTooltip?.position!!.rightOfTop(shipImgTooltip, 0f)
            }

            exoticBandwidthText?.let {
                modifyBandwidthText(it, bandwidth, "CommonOptions", "ExoticBandwidthForShip")
            }
        }

        lastBandwidth = bandwidth
    }

    private fun modifyBandwidthText(
        label: LabelAPI,
        bandwidth: Float,
        translationParent: String,
        translationKey: String
    ) {
        label.text = StringUtils.getTranslation(translationParent, translationKey)
            .format(
                "shipBandwidth",
                BandwidthUtil.getFormattedBandwidthWithName(bandwidth)
            ).toStringNoFormats()

        label.setHighlightColor(Bandwidth.getBandwidthColor(bandwidth))
        label.setHighlight(BandwidthUtil.getFormattedBandwidthWithName(bandwidth))
    }

    private fun setBandwidthUpgradeLabel() {
        bandwidthUpgradeLabel?.let {
            if (mods.baseBandwidth >= Bandwidth.MAX_BANDWIDTH) {
                modifyBandwidthUpgradeLabel(it, -1f, -1f, "ShipListDialog", "BandwidthUpgradePeak")
                bandwidthButton?.isEnabled = false
            } else {
                val marketMult = BandwidthHandler.getMarketBandwidthMult(market)
                val upgradePrice = BandwidthHandler.getBandwidthUpgradePrice(member, mods.baseBandwidth, marketMult)
                val newBandwidth = Bandwidth.BANDWIDTH_STEP * marketMult

                if (member.fleetData.fleet.cargo.credits.get() < upgradePrice) {
                    modifyBandwidthUpgradeLabel(
                        it,
                        newBandwidth,
                        upgradePrice,
                        "BandwidthDialog",
                        "BandwidthUpgradeCostCannotAfford"
                    )
                    bandwidthButton?.isEnabled = false
                } else {
                    modifyBandwidthUpgradeLabel(
                        it,
                        newBandwidth,
                        upgradePrice,
                        "BandwidthDialog",
                        "BandwidthUpgradeCost"
                    )
                    bandwidthButton?.isEnabled = true
                }
            }
        }
    }

    private fun modifyBandwidthUpgradeLabel(
        label: LabelAPI,
        bandwidth: Float,
        upgradePrice: Float,
        translationParent: String,
        translationKey: String
    ) {
        val creditsText = Misc.getDGSCredits(upgradePrice)
        StringUtils.getTranslation(translationParent, translationKey)
            .format("bonusBandwidth", BandwidthUtil.getFormattedBandwidth(bandwidth))
            .format("costCredits", creditsText)
            .format("credits", Misc.getDGSCredits(member.fleetData.fleet.cargo.credits.get()))
            .setAdaptiveHighlights()
            .setLabelText(label)
    }

    private fun doBandwidthUpgrade() {
        val marketMult = BandwidthHandler.getMarketBandwidthMult(market)
        val increase = Bandwidth.BANDWIDTH_STEP * marketMult
        val upgradePrice = BandwidthHandler.getBandwidthUpgradePrice(member, mods.baseBandwidth, marketMult)

        member.fleetData.fleet.cargo.credits.subtract(upgradePrice)

        val newBandwidth = min(mods.baseBandwidth + increase, Bandwidth.MAX_BANDWIDTH)
        mods.setBandwidth(newBandwidth)
        mods.save(member)

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 0.75f)
    }

    fun bandwidthButtonClicked() {
        bandwidthButton?.isChecked = false
        doBandwidthUpgrade()
        setBandwidthUpgradeLabel()
    }
}