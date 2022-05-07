package extrasystemreloaded.systems.exotics.items;

import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.systems.exotics.ExoticSpecialItem;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.systems.exotics.impl.EqualizerCore;

public class EqualizerCoreItem extends ExoticSpecialItem {
    @Override
    public Exotic getExotic() {
        return ExoticsHandler.EXOTICS.get("EqualizerCore");
    }
}
