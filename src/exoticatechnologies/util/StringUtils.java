package exoticatechnologies.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private StringUtils() {
    }

    public static String formatCost(float cost) {
        String costText = "";
        if (cost > 0) {
            costText = String.format("-%s", Misc.getWithDGS(cost));
        } else if (cost < 0) {
            costText = String.format("+%s", Misc.getWithDGS(Math.abs(cost)));
        }
        return costText;
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
     * First ships is of strings that correspond to a "key" in the format formatted like this:
     * ${key}
     * The second ships is of values. If any are a number, they will be formatted using Starsector's number formatting.
     * It will be run through String.valueOf otherwise.
     *
     * @param format the format
     * @param keys the keys
     * @param values the values
     * @return the string
     */
    public static String formatString(String format, List<String> keys, List<String> values) {
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

    private static final Map<Character, Color> adaptiveHighlightCharacterMap = new HashMap<>();
    private static final String regularHighlightCharacterPattern;
    private static final Pattern regularHighlightPattern;
    private static final String adaptiveHighlightCharacterPattern;
    private static final Pattern adaptiveHighlightsPattern;
    private static Color superNegativeColor = new Color(169, 57, 57);
    static {

        adaptiveHighlightCharacterMap.put('*', Misc.getHighlightColor());
        adaptiveHighlightCharacterMap.put('=', Misc.getNegativeHighlightColor());
        adaptiveHighlightCharacterMap.put('&', Misc.getEnergyMountColor());
        adaptiveHighlightCharacterMap.put('^', Misc.getPositiveHighlightColor());
        adaptiveHighlightCharacterMap.put('`', superNegativeColor);
        adaptiveHighlightCharacterMap.put('@', null);

        regularHighlightCharacterPattern = "[*]";
        regularHighlightPattern = Pattern.compile("[*]([^*]*)[*]");

        StringBuilder allCharsBuilder = new StringBuilder();
        for (Character theChar : adaptiveHighlightCharacterMap.keySet()) {
            allCharsBuilder.append(theChar);
        }

        adaptiveHighlightCharacterPattern = String.format("[%s]", allCharsBuilder);
        adaptiveHighlightsPattern = Pattern.compile(String.format("[%s]([^%s]*)[%s]", allCharsBuilder, allCharsBuilder, allCharsBuilder));
    }


    /**
     * can match for positive (*) and negative (=) highlights
     * @param format string to format
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
     * @param format string to get highlights from
     * @return ships of strings to be highlighted
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

    public static LabelAPI addToTooltip(TooltipMakerAPI tooltip, String translated, float pad) {
        Pair<String[], Color[]> highlights = getAdaptiveHighlights(translated);
        return tooltip.addPara(getPad(pad) + translated.replaceAll(adaptiveHighlightCharacterPattern, ""),
                2f,
                highlights.two,
                highlights.one);
    }

    public static LabelAPI addToTooltip(TooltipMakerAPI tooltip, String translated, float pad, Color[] colors) {
        String[] highlights = getHighlights(translated);
        return tooltip.addPara(getPad(pad) + translated.replaceAll(adaptiveHighlightCharacterPattern, ""),
                2f,
                colors,
                highlights);
    }

    public static void addToTextPanel(TextPanelAPI textPanel, String translated) {
        Pair<String[], Color[]> highlights = getAdaptiveHighlights(translated);
        textPanel.addPara(translated.replaceAll(adaptiveHighlightCharacterPattern, "").replaceAll("%%", "%"));
        textPanel.highlightInLastPara(highlights.one);
        textPanel.setHighlightColorsInLastPara(highlights.two);
    }

    public static void addToTextPanel(TextPanelAPI textPanel, String translated, Color[] highlightColors) {
        String[] highlights = getHighlights(translated);
        textPanel.addPara(translated.replaceAll(regularHighlightCharacterPattern, "").replaceAll("%%", "%"));
        textPanel.highlightInLastPara(highlights);
        textPanel.setHighlightColorsInLastPara(highlightColors);
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Translation {
        protected final String scope;
        protected final String key;
        protected final List<String> formats = new ArrayList<>();
        protected List<String> values = new ArrayList<>();
        protected Map<Integer, Color> colors = new HashMap<>();

        public Translation format(String flag, Object value) {
            formats.add(flag);
            values.add(StringUtils.formatValue(value));

            return this;
        }

        public Translation formatWithColorIfModified(String flag, Object value, Object base, Color modifiedColor) {
            if (value.equals(base) || (value instanceof Number && (((Number) value).floatValue()) == (((Number) base).floatValue()))) {
                return format(flag, value);
            } else {
                return format(flag, value, modifiedColor);
            }
        }

        public Translation format(String flag, Object value, Color color) {
            if (color == null) {
                return format(flag, value);
            }

            formats.add(flag);
            values.add(StringUtils.formatValue(value));
            colors.put(values.size() - 1, color);

            return this;
        }

        public Translation format(String flag, Conditional value) {
            formats.add(flag);
            values.add(StringUtils.formatValue(value.get()));

            return this;
        }

        public Translation formatFloat(String flag, Number value) {
            formats.add(flag);
            values.add(Misc.getRoundedValue(value.floatValue()));

            return this;
        }

        public Translation formatWithModifier(String flag, Number value) {
            formats.add(flag);
            values.add(getModifier(value) + value.intValue());

            return this;
        }

        public Translation formatFloatWithModifier(String flag, Number value) {
            formats.add(flag);
            values.add(getModifier(value) + getFloat(value));

            return this;
        }

        public Translation formatMult(String flag, Number value) {
            formats.add(flag);
            values.add(getFloat(value.floatValue()) + "x");

            return this;
        }

        public Translation formatMultWithModifier(String flag, Number value) {
            formats.add(flag);
            values.add(getModifier(value) + getFloat(value.floatValue()) + "x");

            return this;
        }

        public Translation formatWithOneDecimalAndModifier(String flag, Conditional cond) {
            formats.add(flag);

            Float value = Float.valueOf(cond.get());

            values.add(getFloatWithModifier(value));

            return this;
        }

        public Translation formatPercWithModifier(String flag, Number value) {
            formats.add(flag);
            values.add(getModifier(value) + getFloat(value.floatValue()) + "%%");

            return this;
        }

        public Translation formatPercWithOneDecimalAndModifier(String flag, Number value) {
            formats.add(flag);
            values.add(getFloatWithModifier(value.floatValue()) + "%%");

            return this;
        }

        public Translation formatWithOneDecimalAndModifier(String flag, Number value) {
            formats.add(flag);
            values.add(getFloatWithModifier(value.floatValue()));

            return this;
        }

        private static String getFloat(Number value) {
            return Misc.getRoundedValue(value.floatValue());
        }

        private static String getModifier(Number value) {
            String prefix;
            if (value.floatValue() > 0f) {
                prefix = "+";
            } else {
                prefix = "";
            }

            return prefix;
        }

        private static String getFloatWithModifier(Number value) {
            return getModifier(value) + Misc.getRoundedValueMaxOneAfterDecimal(value.floatValue());
        }

        private static String getFloatLessRoundWithModifier(Number value) {
            return getModifier(value) + Misc.getRoundedValue(value.floatValue());
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

        public String toStringNoFormats() {
            return this.toString().replaceAll(adaptiveHighlightCharacterPattern, "");
        }

        public LabelAPI addToTooltip(TooltipMakerAPI tooltip) {
            return this.addToTooltip(tooltip, 0f);
        }

        public LabelAPI addToTooltip(TooltipMakerAPI tooltip, Color... colors) {
            return this.addToTooltip(tooltip, 0f, colors);
        }

        public LabelAPI addToTooltip(TooltipMakerAPI tooltip, float pad) {
            if (!colors.isEmpty()) {
                return this.addToTooltip(tooltip, getColorArray());
            } else {
                return StringUtils.addToTooltip(tooltip, this.toString(), pad);
            }
        }

        public LabelAPI addToTooltip(TooltipMakerAPI tooltip, float pad, Color... colors) {
            return StringUtils.addToTooltip(tooltip, this.toString(), pad, colors);
        }

        public LabelAPI addToTooltip(TooltipMakerAPI tooltip, UIComponentAPI positionBelowThis) {
            LabelAPI label = this.addToTooltip(tooltip, 0);
            UIComponentAPI prev = tooltip.getPrev();
            prev.getPosition().belowLeft(positionBelowThis, 2);
            return label;
        }

        public Translation setAdaptiveHighlights() {
            Pair<String[], Color[]> valuesToColors = getAdaptiveHighlights(getString(scope, key));

            for (int i = 0; i < valuesToColors.two.length; i++) {
                if (!colors.containsKey(i)) {
                    colors.put(i, valuesToColors.two[i]);
                }
            }

            return this;
        }

        public void setLabelText(LabelAPI label) {
            setAdaptiveHighlights();

            label.setText(this.toStringNoFormats());
            label.setHighlight(values.toArray(new String[0]));
            label.setHighlightColors(getColorArray());
        }

        public Color[] getColorArray() {
            setAdaptiveHighlights();
            return new ArrayList<>(colors.values()).toArray(new Color[0]);
        }

        public void addToTextPanel(TextPanelAPI textPanel) {
            if (!colors.isEmpty()) {
                this.addToTextPanel(textPanel, getColorArray());
            } else {
                StringUtils.addToTextPanel(textPanel, this.toString());
            }
        }

        public void addToTextPanel(TextPanelAPI textPanel, Color... highlightColors) {
            StringUtils.addToTextPanel(textPanel, this.toString(), highlightColors);
        }
    }

    public interface Conditional {
        String get();
    }
}
