package exoticatechnologies.ui2.impl.mods.bandwidth

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthHandler
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.refit.RefitButtonAdder
import exoticatechnologies.ui2.util.StringTooltip
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.mods.ExoticaPanel
import exoticatechnologies.util.StringUtils
import kotlin.math.min

class BandwidthHeader(context: ExoticaPanelContext) : ExoticaPanel(context) {
    var lastCredits: Float = -1f
    var shipImgTooltip: TooltipMakerAPI? = null
    var shipTextTooltip: TooltipMakerAPI? = null
    var shipBandwidthText: LabelAPI? = null
    var placeHelpTextNextToThisComponent: UIComponentAPI? = null
    var usedBandwidthText: LabelAPI? = null
    var bandwidthHelpText: LabelAPI? = null
    var bandwidthTooltip: TooltipMakerAPI? = null
    var bandwidthUpgradeLabel: LabelAPI? = null
    var bandwidthButton: ButtonAPI? = null

    override fun advancePanel(amount: Float) {
        if (lastCredits != Global.getSector().playerFleet.cargo.credits.get()) {
            refreshPanel()
        }
    }

    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        lastCredits = Global.getSector().playerFleet.cargo.credits.get()

        val shipNameColor = Misc.getBasePlayerColor()

        // Ship image with tooltip of the ship class
        val shipImg = menuPanel.createUIElement(innerHeight, innerHeight, false)
        shipImgTooltip = shipImg
        shipImg.addShipList(1, 1, innerHeight, Misc.getBasePlayerColor(), mutableListOf(member), 0f)
        menuPanel.addUIElement(shipImg).inTL(innerPadding, innerPadding)

        // Ship name, class, bandwidth
        val shipText = menuPanel.createUIElement(innerWidth - innerHeight - innerPadding, 22f * 3f, false)
        shipTextTooltip = shipText
        shipText.setParaOrbitronLarge()
        shipText.addPara(
            "${member?.shipName} (${member?.hullSpec?.nameWithDesignationWithDashClass})",
            shipNameColor,
            0f
        )
        shipText.setParaFontDefault()

        shipBandwidthText = shipText.addPara("", 0f)
        usedBandwidthText = shipText.addPara("", 0f)
        placeHelpTextNextToThisComponent = shipText.prev

        bandwidthHelpText = shipText.addPara("[?]", Misc.getDarkHighlightColor(), 3f)
        val bandwidthHelpString = StringUtils.getTranslation("Bandwidth", "BandwidthHelp")
            .format("bandwidthLimit", BandwidthUtil.getFormattedBandwidth(Bandwidth.MAX_BANDWIDTH))
            .toStringNoFormats()

        shipText.addTooltipToPrevious(
            StringTooltip(shipText, bandwidthHelpString),
            TooltipMakerAPI.TooltipLocation.BELOW
        )

        setBandwidthText()

        menuPanel.addUIElement(shipText).rightOfTop(shipImg, innerPadding)

        val bandwidthHeight = innerHeight - shipText.position.height
        bandwidthTooltip = menuPanel.createUIElement(innerWidth - innerHeight, bandwidthHeight, false)
            .also { tooltip ->
                bandwidthUpgradeLabel = tooltip.addPara("", 0f)
                bandwidthButton = tooltip.addButton(
                    StringUtils.getString("Bandwidth", "BandwidthPurchase"), "test",
                    Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.C2_MENU, 72F, 22F, 3F
                ).also {
                    onClick(it) {
                        bandwidthButtonClicked()
                    }
                }
            }

        setBandwidthUpgradeLabel()

        menuPanel.addUIElement(bandwidthTooltip).belowLeft(shipText, innerPadding)

        mods?.let {
            it.addListener("bandwidthListener") {
                refreshPanel()
            }
        }
    }

    private fun setBandwidthText() {
        val baseBandwidth = member?.let { mods?.getBaseBandwidth(it) } ?: -1f
        val bandwidthWithExotics = member?.let { mods?.getBandwidthWithExotics(it) } ?: -1f
        val usedBandwidth = mods?.getUsedBandwidth() ?: -1f

        if (baseBandwidth != bandwidthWithExotics) {
            //something installed that increases bandwidth. bandwidth value is exotic bandwidth
            StringUtils.getTranslation("Bandwidth", "BandwidthWithExoticsForShip")
                .format(
                    "shipBandwidth",
                    BandwidthUtil.getFormattedBandwidth(baseBandwidth),
                    Bandwidth.getColor(baseBandwidth)
                )
                .format(
                    "exoticBandwidth",
                    "+" + BandwidthUtil.getFormattedBandwidth(bandwidthWithExotics - baseBandwidth),
                    Misc.getPositiveHighlightColor()
                )
                .setLabelText(shipBandwidthText)
        } else {
            StringUtils.getTranslation("Bandwidth", "BandwidthForShip")
                .format(
                    "shipBandwidth",
                    BandwidthUtil.getFormattedBandwidth(baseBandwidth),
                    Bandwidth.getColor(baseBandwidth)
                )
                .setLabelText(shipBandwidthText)
        }

        StringUtils.getTranslation("Bandwidth", "BandwidthUsedWithMax")
            .format(
                "usedBandwidth",
                BandwidthUtil.getFormattedBandwidth(usedBandwidth),
                Misc.getHighlightColor()
            )
            .format(
                "allBandwidth",
                BandwidthUtil.getFormattedBandwidthWithName(bandwidthWithExotics),
                Bandwidth.getColor(bandwidthWithExotics)
            )
            .setLabelText(usedBandwidthText)

        bandwidthHelpText?.let {
            it.position
                .belowRight(placeHelpTextNextToThisComponent!!, 3f)
                .setXAlignOffset(4 + it.computeTextWidth(usedBandwidthText!!.text))
                .setYAlignOffset(it.computeTextHeight(usedBandwidthText!!.text))
        }
    }

    private fun setBandwidthUpgradeLabel() {
        if (market == null) {
            StringUtils.getTranslation("CommonOptions", "MustBeDockedAtMarket")
                .setLabelText(bandwidthUpgradeLabel)
            bandwidthUpgradeLabel?.setColor(Misc.getNegativeHighlightColor())
            bandwidthButton?.isEnabled = false
            return
        }

        mods?.let { mods ->
            bandwidthUpgradeLabel?.let {
                if (mods.getBaseBandwidth() >= Bandwidth.MAX_BANDWIDTH) {
                    modifyBandwidthUpgradeLabel(it, -1f, -1f, "Bandwidth", "BandwidthUpgradePeak")
                    bandwidthButton?.isEnabled = false
                } else {
                    val marketMult = BandwidthHandler.getMarketBandwidthMult(market)
                    val upgradePrice =
                        BandwidthHandler.getBandwidthUpgradePrice(member, mods.getBaseBandwidth(), marketMult)
                    val newBandwidth = Bandwidth.BANDWIDTH_STEP * marketMult

                    if (Global.getSector().playerFleet.cargo.credits.get() < upgradePrice) {
                        modifyBandwidthUpgradeLabel(
                            it,
                            newBandwidth,
                            upgradePrice,
                            "Bandwidth",
                            "BandwidthUpgradeCostCannotAfford"
                        )
                        bandwidthButton?.isEnabled = false
                    } else {
                        modifyBandwidthUpgradeLabel(
                            it,
                            newBandwidth,
                            upgradePrice,
                            "Bandwidth",
                            "BandwidthUpgradeCost"
                        )
                        bandwidthButton?.isEnabled = true
                    }
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
            .format("credits", Misc.getDGSCredits(Global.getSector().playerFleet.cargo.credits.get()))
            .setAdaptiveHighlights()
            .setLabelText(label)
    }

    private fun doBandwidthUpgrade() {
        mods?.let { mods ->
            val marketMult = BandwidthHandler.getMarketBandwidthMult(market)
            val increase = Bandwidth.BANDWIDTH_STEP * marketMult
            val upgradePrice = BandwidthHandler.getBandwidthUpgradePrice(member, mods.getBaseBandwidth(), marketMult)

            Global.getSector().playerFleet.cargo.credits.subtract(upgradePrice)

            val newBandwidth = min(mods.getBaseBandwidth() + increase, Bandwidth.MAX_BANDWIDTH)
            mods.setBaseBandwidth(newBandwidth)
            ShipModLoader.set(member!!, variant!!, mods)

            if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT) {
                RefitButtonAdder.requiresVariantUpdate = true
            }

            Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 0.5f)
        }
    }

    fun bandwidthButtonClicked() {
        bandwidthButton?.isChecked = false
        doBandwidthUpgrade()
    }
}