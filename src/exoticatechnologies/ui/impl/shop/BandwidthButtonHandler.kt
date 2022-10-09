package exoticatechnologies.ui.impl.shop

import exoticatechnologies.ui.ButtonHandler

class BandwidthButtonHandler(val bandwidthPanel: ShipHeaderUIPanelPlugin): ButtonHandler() {
    override fun checked() {
        bandwidthPanel.bandwidthButtonClicked()
    }
}