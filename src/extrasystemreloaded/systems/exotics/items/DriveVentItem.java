package extrasystemreloaded.systems.exotics.items;

import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.systems.exotics.ExoticSpecialItem;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.systems.exotics.impl.DriveFluxVent;

public class DriveVentItem extends ExoticSpecialItem {
    @Override
    public Exotic getExotic() {
        return ExoticsHandler.EXOTICS.get("DriveFluxVent");
    }
}
