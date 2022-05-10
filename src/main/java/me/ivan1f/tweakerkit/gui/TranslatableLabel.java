package me.ivan1f.tweakerkit.gui;

import fi.dy.masa.malilib.gui.widgets.WidgetLabel;
import fi.dy.masa.malilib.util.StringUtils;
import me.ivan1f.tweakerkit.utils.StringUtil;

import java.util.Arrays;
import java.util.function.Function;

public class TranslatableLabel extends WidgetLabel {
    public static final double TRANSLATION_SCALE = 0.65;
    private final String[] originalLines;
    private final boolean showOriginalLines;

    public TranslatableLabel(int x, int y, int width, int height, int textColor, String[] text, String[] originalLines, Function<String, String> lineModifier) {
        super(x, y, width, height, textColor, text);
        this.originalLines = originalLines;
        boolean showOriginalLines = false;
        for (int i = 0; i < this.originalLines.length; i++) {
            String linesToDisplay = this.labels.get(i);
            if (!this.originalLines[i].equals(StringUtil.removeFormattingCode(linesToDisplay))) {
                showOriginalLines = true;
            }
            this.labels.set(i, lineModifier.apply(linesToDisplay));
        }
        this.showOriginalLines = showOriginalLines;
    }

    public TranslatableLabel(int x, int y, int width, int height, int textColor, String[] text, String[] originalLines) {
        this(x, y, width, height, textColor, text, originalLines, line -> line);
    }

    public TranslatableLabel(int x, int y, int width, int height, int textColor, String[] text, String originalLine, Function<String, String> lineModifier) {
        this(x, y, width, height, textColor, text, new String[]{originalLine}, lineModifier);
    }

    public static boolean willShowOriginalLines(String[] displayLines, String[] originalLines) {
        return !Arrays.equals(
                originalLines,
                Arrays.stream(displayLines).
                        map(StringUtils::translate).
                        map(StringUtil::removeFormattingCode).
                        toArray(String[]::new)
        );
    }

    public String[] getOriginalLines() {
        return originalLines;
    }

    public boolean shouldShowOriginalLines() {
        return showOriginalLines;
    }
}
