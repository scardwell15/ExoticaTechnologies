package extrasystemreloaded.systems.exotics.items;

import extrasystemreloaded.systems.exotics.Exotic;
import extrasystemreloaded.systems.exotics.ExoticSpecialItem;
import extrasystemreloaded.systems.exotics.ExoticsHandler;
import extrasystemreloaded.systems.exotics.impl.PhasefieldEngine;

public class PhasefieldEngineItem extends ExoticSpecialItem {
    @Override
    public Exotic getExotic() {
        return ExoticsHandler.EXOTICS.get("PhasefieldEngine");
    }
}
