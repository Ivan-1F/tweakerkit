package me.ivan1f.tweakerkit.gui;

import fi.dy.masa.malilib.gui.widgets.WidgetLabel;
import me.ivan1f.tweakerkit.utils.StringUtil;

public class TranslatableLabel extends WidgetLabel {
    public static final double TRANSLATION_SCALE = 0.65;
    private final String[] originalLines;
    private final boolean showOriginalLines;

    public TranslatableLabel(int x, int y, int width, int height, int textColor, String[] text, String[] originalLines) {
        super(x, y, width, height, textColor, text);
        this.originalLines = originalLines;
        boolean showOriginalLines = false;
        for (int i = 0; i < this.originalLines.length; i++) {
            String linesToDisplay = this.labels.get(i);
            if (!this.originalLines[i].equals(StringUtil.removeFormattingCode(linesToDisplay))) {
                showOriginalLines = true;
            }
//            this.labels.set(i, lineModifier.apply(linesToDisplay));
        }
        this.showOriginalLines = showOriginalLines;
    }

    public TranslatableLabel(int x, int y, int width, int height, int textColor, String[] text, String originalLine) {
        this(x, y, width, height, textColor, text, new String[]{originalLine});
    }

    public String[] getOriginalLines() {
        return originalLines;
    }

    public boolean shouldShowOriginalLines() {
        return showOriginalLines;
    }
}
