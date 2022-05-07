package extrasystemreloaded.systems.bandwidth;

import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class BandwidthUtil {
    public static String getFormattedBandwidth(float shipBandwidth) {
        return getRoundedBandwidth(shipBandwidth);
    }

    public static String getFormattedBandwidthWithName(float shipBandwidth) {
        return getRoundedBandwidth(shipBandwidth) + " " + Bandwidth.getBandwidthName(shipBandwidth);
    }

    public static String getRoundedBandwidth(float shipBandwidth) {
        return Misc.getRoundedValueMaxOneAfterDecimal(shipBandwidth) + " TB/s";
    }
}
