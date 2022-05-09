package me.ivan1f.tweakerkit.gui;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class TweakerKitConfigGui extends GuiConfigsBase {
    public Map<GuiFeatureType, Boolean> features = Maps.newHashMap();

    public TweakerKitConfigGui(int listX, int listY, String modId, @Nullable Screen parent, String titleKey, Object... args) {
        super(listX, listY, modId, parent, titleKey, args);
    }

    public static Pair<Integer, Integer> adjustWidths(int guiWidth, int maxTextWidth) {
        int labelWidth;
        int panelWidth = 190;
        guiWidth -= 75;

        // tweak label width first, to make sure the panel is not too close or too far from the label
        labelWidth = MathHelper.clamp(guiWidth - panelWidth, maxTextWidth - 5, maxTextWidth + 100);
        // decrease the panel width if space is not enough
        panelWidth = MathHelper.clamp(guiWidth - labelWidth, 100, panelWidth);
        // decrease the label width for a bit if space is still way not enough (the label text might overlap with the panel now)
        labelWidth = MathHelper.clamp(guiWidth - panelWidth + 25, labelWidth - Math.max((int) (maxTextWidth * 0.4), 30), labelWidth);

        // just in case
        labelWidth = Math.max(labelWidth, 0);
        panelWidth = Math.max(panelWidth, 0);

        return Pair.of(labelWidth, panelWidth);
    }

    public boolean isFeatureEnabled(GuiFeatureType type) {
        return this.features.getOrDefault(type, false);
    }
}
