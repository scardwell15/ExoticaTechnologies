package exoticatechnologies.ui.impl.shop

import exoticatechnologies.ui.ButtonHandler

class BandwidthButtonHandler(val bandwidthPanel: ShipHeaderUIPlugin): ButtonHandler() {
    override fun checked() {
        bandwidthPanel.bandwidthButtonClicked()
    }
}