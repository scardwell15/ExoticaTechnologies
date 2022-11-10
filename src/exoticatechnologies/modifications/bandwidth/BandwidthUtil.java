package exoticatechnologies.modifications.bandwidth;

import com.fs.starfarer.api.util.Misc;

public class BandwidthUtil {
    public static String getFormattedBandwidth(float shipBandwidth) {
        return getRoundedBandwidth(shipBandwidth);
    }

    public static String getFormattedBandwidthWithName(float shipBandwidth) {
        return getRoundedBandwidth(shipBandwidth) + " " + Bandwidth.getName(shipBandwidth);
    }

    public static String getRoundedBandwidth(float shipBandwidth) {
        return Misc.getRoundedValueMaxOneAfterDecimal(shipBandwidth) + " TB/s";
    }
}
