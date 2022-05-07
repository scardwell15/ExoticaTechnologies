package extrasystemreloaded.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private StringUtils() {
    }

    public static String getString(String parent, String key) {
        try {
            return Global.getSettings().getString(parent, key);
        } catch (Throwable th) {
            return String.format("Failed to get String (parent: %s, key: %s)", parent, key);
        }
    }

    public static Translation getTranslation(String parent, String key) {
        return new Translation(parent, key);
    }

    /**
     * Formats a string using two lists.
     * First list is of strings that correspond to a "key" in the format formatted like this:
     * ${key}
     * The second list is of values. If any are a number, they will be formatted using Starsector's number formatting.
     * It will be run through String.valueOf otherwise.
     *
     * @param format the format
     * @param keys the keys
     * @param values the values
     * @return the string
     */
    public static String formatString(String format, List<String> keys, List<Object> values) {
        if (keys.size() != values.size()) {
            return null;
        }

        for(int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = values.get(i);

            format = format.replace(getKey(key), formatValue(value));
        }

        return format;
    }

    private static String getKey(String key) {
        return new StringBuilder("${").append(key).append("}").toString();
    }

    private static String formatValue(Object value) {
        if(value instanceof Float) {
            return Misc.getFormat().format(value);
        } else {
            return String.valueOf(value);
        }
    }

    private static Map<Character, Color> adaptiveHighlightCharacterMap = new HashMap<>();
    static {
        adaptiveHighlightCharacterMap.put('*', Misc.getHighlightColor());
        adaptiveHighlightCharacterMap.put('=', Misc.getNegativeHighlightColor());
    }

    private static Pattern regularHighlightPattern = Pattern.compile("[\\*]([^\\*]*)[\\*]");
    private static Pattern adaptiveHighlightsPattern = Pattern.compile("[=\\*]([^=\\*]*)[=\\*]");

    /**
     * can match for positive (*) and negative (=) highlights
     * @param format
     * @return pair that contains strings and their highlights
     */
    private static Pair<String[], Color[]> getAdaptiveHighlights(String format) {
        List<String> highlights = new ArrayList<>();
        List<Color> colors = new ArrayList<>();

        Matcher matcher = adaptiveHighlightsPattern.matcher(format);
        while(matcher.find()) {
            String inside = format.substring(matcher.start() + 1, matcher.end() - 1);

            highlights.add(inside.replace("%%", "%"));
            colors.add(adaptiveHighlightCharacterMap.get(matcher.group().charAt(0)));
        }
        return new Pair<>(highlights.toArray(new String[0]), colors.toArray(new Color[0]));
    }

    /**
     * only matches positive (*) highlights
     * @param format
     * @return list of strings to be highlighted
     */
    private static String[] getHighlights(String format) {
        List<String> highlights = new ArrayList<>();

        Matcher matcher = regularHighlightPattern.matcher(format);
        while(matcher.find()) {
            String inside = format.substring(matcher.start() + 1, matcher.end() - 1);
            highlights.add(inside.replace("%%", "%"));
        }
        return highlights.toArray(new String[0]);
    }

    private static String getPad(float pad) {
        StringBuilder padding = new StringBuilder();
        for(int i = 0; i < pad; i++) {
            padding.append(" ");
        }
        return padding.toString();
    }

    public static void addToTooltip(TooltipMakerAPI tooltip, String translated, float pad) {
        Pair<String[], Color[]> highlights = getAdaptiveHighlights(translated);
        tooltip.addPara(getPad(pad) + translated.replaceAll("[=\\*]", ""),
                2f,
                highlights.two,
                highlights.one);
    }

    public static void addToTooltip(TooltipMakerAPI tooltip, String translated, float pad, Color[] colors) {
        String[] highlights = getHighlights(translated);
        tooltip.addPara(getPad(pad) + translated.replaceAll("[\\*]", ""),
                2f,
                colors,
                highlights);
    }

    public static void addToTextPanel(TextPanelAPI textPanel, String translated) {
        Pair<String[], Color[]> highlights = getAdaptiveHighlights(translated);
        textPanel.addPara(translated.replaceAll("[=\\*]", "").replaceAll("%%", "%"));
        textPanel.highlightInLastPara(highlights.one);
        textPanel.setHighlightColorsInLastPara(highlights.two);
    }

    public static void addToTextPanel(TextPanelAPI textPanel, String translated, Color[] highlightColors) {
        String[] highlights = getHighlights(translated);
        textPanel.addPara(translated.replaceAll("\\*", "").replaceAll("%%", "%"));
        textPanel.highlightInLastPara(highlights);
        textPanel.setHighlightColorsInLastPara(highlightColors);
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Translation {
        protected final String scope;
        protected final String key;
        protected List<String> formats = new ArrayList<>();
        protected List<Object> values = new ArrayList<>();
        protected List<Color> colors = new ArrayList<>();

        public Translation format(String flag, Object value) {
            formats.add(flag);
            values.add(StringUtils.formatValue(value));

            return this;
        }

        public Translation format(String flag, Object value, Color color) {
            formats.add(flag);
            values.add(StringUtils.formatValue(value));
            colors.add(color);

            return this;
        }

        public Translation format(String flag, Conditional value) {
            formats.add(flag);
            values.add(StringUtils.formatValue(value.get()));

            return this;
        }

        public Translation formatWithOneDecimalAndModifier(String flag, Conditional cond) {
            formats.add(flag);

            Float value = Float.valueOf(cond.get());

            values.add(getFloatWithModifier(value));

            return this;
        }

        public Translation formatWithOneDecimalAndModifier(String flag, Number value) {
            formats.add(flag);
            values.add(getFloatWithModifier(value.floatValue()));

            return this;
        }

        public Translation formatWithModifier(String flag, Number value) {
            formats.add(flag);
            values.add(value.intValue());

            return this;
        }

        private static String getFloatWithModifier(Number value) {
            String prefix;
            if (value.floatValue() > 0f) {
                prefix = "+";
            } else {
                prefix = "";
            }

            return prefix + Misc.getRoundedValueMaxOneAfterDecimal(value.floatValue());
        }

        public String toString() {
            try {
                String format = formatString(getString(scope, key), formats, values);

                if (format == null) {
                    return String.format("Failed to get String (parent: %s, key: %s) because keys and values were different sizes.", scope, key);
                }

                return format;
            } catch (Throwable th) {
                return String.format("Failed to get String (parent: %s, key: %s)", scope, key);
            }
        }

        public void addToTooltip(TooltipMakerAPI tooltip) {
            this.addToTooltip(tooltip, 0f);
        }

        public void addToTooltip(TooltipMakerAPI tooltip, Color... colors) {
            this.addToTooltip(tooltip, 0f, colors);
        }

        public void addToTooltip(TooltipMakerAPI tooltip, float pad) {
            if (!colors.isEmpty()) {
                this.addToTooltip(tooltip, colors.toArray(new Color[0]));
            } else {
                StringUtils.addToTooltip(tooltip, this.toString(), pad);
            }
        }

        public void addToTooltip(TooltipMakerAPI tooltip, float pad, Color... colors) {
            StringUtils.addToTooltip(tooltip, this.toString(), pad, colors);
        }

        public void addToTextPanel(TextPanelAPI textPanel) {
            if (!colors.isEmpty()) {
                this.addToTextPanel(textPanel, colors.toArray(new Color[0]));
            } else {
                StringUtils.addToTextPanel(textPanel, this.toString());
            }
        }

        public void addToTextPanel(TextPanelAPI textPanel, Color... highlightColors) {
            StringUtils.addToTextPanel(textPanel, this.toString(), highlightColors);
        }
    }

    public static interface Conditional {
        String get();
    }
}
