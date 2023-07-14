package exoticatechnologies.modifications.bandwidth;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import exoticatechnologies.modifications.ShipModFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.awt.Color;
import java.util.*;

@Log4j
@RequiredArgsConstructor
public enum Bandwidth {
    TERRIBLE(0f, "terrible", new Color(200,100,100), 50),
    CRUDE(50f, "crude", new Color(200,150,100), 60),
    POOR(75f, "poor", new Color(210,210,120), 95),
    NORMAL(95f, "normal", new Color(200,200,200), 150),
    GOOD(125f, "good", new Color(100,200,110), 110),
    SUPERIOR(150f, "superior", new Color(75,125,255), 16),
    PRISTINE(200f, "pristine", new Color(150,100,200), 8),
    ULTIMATE(250f, "ultimate", new Color(255,100,255), 4),
    PERFECT(300f, "perfect", new Color(200,255,255), 1),
    UNKNOWN(400f, "unknown", new Color(255, 153, 0), 0);

    public static final String BANDWIDTH_RESOURCE = "Bandwidth";
    public static final float BANDWIDTH_STEP = 5f;
    public static final float MAX_BANDWIDTH = PERFECT.bandwidth;
    private static Map<Float, Bandwidth> BANDWIDTH_MAP = null;
    private static final List<Bandwidth> BANDWIDTH_LIST = Arrays.asList(values());
    @Getter public final float bandwidth;
    @Getter private final String key;
    @Getter private final Color color;
    @Getter private final int weight;

    public float getRandomInRange() {
        if (this == PERFECT) {
            return bandwidth;
        }

        if(BANDWIDTH_LIST.indexOf(this) == BANDWIDTH_LIST.size() - 1) {
            return bandwidth;
        }

        float nextBandwidth = BANDWIDTH_LIST.get(BANDWIDTH_LIST.indexOf(this) + 1).getBandwidth();
        return Math.round(ShipModFactory.getRandomNumberInRange(bandwidth, nextBandwidth));
    }

    public static Bandwidth generate() {
        int highNumber = 0;
        for(Bandwidth b : values()) {
            highNumber += b.getWeight();
        }

        int chosen = ShipModFactory.getRandomNumberInRange(0, highNumber);
        for(Bandwidth b : values()) {
            chosen -= b.getWeight();

            if(chosen <= 0) {
                return b;
            }
        }
        return NORMAL;
    }

    public static Bandwidth generate(float mult) {
        return getPicker(mult).pick(ShipModFactory.random);
    }

    public static WeightedRandomPicker<Bandwidth> getPicker(float mult) {
        WeightedRandomPicker<Bandwidth> picker = new WeightedRandomPicker<>();

        Bandwidth[] values = values();
        for (int i = 0; i < values.length; i++) {
            Bandwidth b = values[i];

            int weight = b.weight;
            if (i < values.length / 2) {
                weight *= (1 - (mult - 1) / 2);
            } else {
                weight *= (1 + (mult - 1) / 2);
            }

            if (weight > 0) {
                picker.add(b, weight);
            }
        }

        return picker;
    }

    public static Map<Float, Bandwidth> getBandwidthMap() {
        if(BANDWIDTH_MAP == null) {
            BANDWIDTH_MAP = new LinkedHashMap<>();
            for(Bandwidth b : values()) {
                BANDWIDTH_MAP.put(b.getBandwidth(), b);
            }
        }
        return BANDWIDTH_MAP;
    }

    public static String getName(float arg) {
        return Global.getSettings().getString("BandwidthName", getBandwidthDef(arg).getKey());
    }

    public static Color getColor(float arg){
        return getBandwidthDef(arg).getColor();
    }

    private static Bandwidth getBandwidthDef(float bandwidth) {
        float winningBandwidth = 0f;
        Bandwidth returnedDef = TERRIBLE;

        for(Bandwidth b : values()) {
            float defBandwidth = b.getBandwidth();

            if(defBandwidth >= winningBandwidth && bandwidth >= defBandwidth) {
                returnedDef = b;
                winningBandwidth = defBandwidth;
            }
        }

        return returnedDef;
    }
}
