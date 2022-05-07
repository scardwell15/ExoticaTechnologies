package extrasystemreloaded.util;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;

import java.text.DecimalFormat;

public class StatUtils {
    private static final DecimalFormat FLOATING_FORMAT = new DecimalFormat("#");
    private static final DecimalFormat FLOATING_FORMAT_UNROUNDED = new DecimalFormat("#.##");

    private StatUtils() {
    }

    public static void setStatPercent(StatBonus stat, String buffId, float mult) {
        if(mult < 0) {
            stat.modifyMult(buffId, (1f - Math.abs(mult / 100f)));
        } else {
            stat.modifyPercent(buffId, mult);
        }
    }

    public static void setStatPercent(MutableStat stat, String buffId, float mult) {
        if(mult < 0) {
            stat.modifyMult(buffId, (1f - Math.abs(mult / 100f)));
        } else {
            stat.modifyPercent(buffId, mult);
        }
    }

    public static void setStatPercent(StatBonus stat, String buffId, int level, float mult, int maxLevel) {
        if(mult < 0) {
            stat.modifyMult(buffId, Math.max(0f, 1f - Math.abs((mult * level / maxLevel) / 100f)));
        } else {
            stat.modifyPercent(buffId, mult * level / maxLevel);
        }
    }

    public static void setStatPercent(MutableStat stat, String buffId, int level, float mult, int maxLevel) {
        if(mult < 0) {
            stat.modifyMult(buffId, (1f - Math.abs((mult * level / maxLevel) / 100f)));
        } else {
            stat.modifyPercent(buffId, mult * level / maxLevel);
        }
    }

    public static void setStatMult(StatBonus stat, String buffId, float mult) {
        stat.modifyMult(buffId, mult);
    }

    public static void setStatMult(MutableStat stat, String buffId, float mult) {
        stat.modifyMult(buffId, mult);
    }

    public static void setStatMult(StatBonus stat, String buffId, int level, float mult, float maxLevel) {
        float multBonus = mult / 100f * level / maxLevel;
        stat.modifyMult(buffId, 1f + multBonus);
    }

    public static void setStatMult(MutableStat stat, String buffId, int level, float mult, int maxLevel) {
        stat.modifyMult(buffId, 1f + mult / 100f * level / maxLevel);
    }

    public static String formatFloatUnrounded(float theFloat) {
        return FLOATING_FORMAT_UNROUNDED.format(theFloat);
    }

    public static String formatFloat(float theFloat) {
        return FLOATING_FORMAT.format(theFloat);
    }
}
