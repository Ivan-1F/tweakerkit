package me.ivan1f.tweakerkit.mixins.gui;

import com.mojang.datafixers.util.Pair;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import me.ivan1f.tweakerkit.gui.FeatureConfig;
import me.ivan1f.tweakerkit.gui.TranslatableLabel;
import me.ivan1f.tweakerkit.gui.TweakerKitConfigGui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(WidgetListConfigOptions.class)
public abstract class WidgetListConfigOptionsMixin extends WidgetListConfigOptionsBase<GuiConfigsBase.ConfigOptionWrapper, WidgetConfigOption> {
    @Shadow(remap = false)
    @Final
    protected GuiConfigsBase parent;

    public WidgetListConfigOptionsMixin(int x, int y, int width, int height, int configWidth) {
        super(x, y, width, height, configWidth);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isTweakerKitConfigGui() {
        return this.parent instanceof TweakerKitConfigGui;
    }

    private <T> T getFeatureValue(FeatureConfig.Key<T> key) {
        return ((TweakerKitConfigGui) this.parent).getFeatureValue(key);
    }

    @ModifyVariable(
            method = "getMaxNameLengthWrapped",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;",
                    remap = false
            ),
            remap = false
    )
    private int updateMaxNameLengthIfUsingTweakerKitOptionLabelAndShowsOriginalText$tweakerkit(int maxWidth, List<GuiConfigsBase.ConfigOptionWrapper> wrappers) {
        if (!isTweakerKitConfigGui()) return maxWidth;
        if (getFeatureValue(FeatureConfig.TRANSLATABLE_LABEL)) {
            for (GuiConfigsBase.ConfigOptionWrapper wrapper : wrappers) {
                if (wrapper.getType() == GuiConfigsBase.ConfigOptionWrapper.Type.CONFIG) {
                    IConfigBase config = Objects.requireNonNull(wrapper.getConfig());
                    maxWidth = Math.max(maxWidth, this.getStringWidth(config.getConfigGuiDisplayName()));
                    if (TranslatableLabel.willShowOriginalLines(new String[]{config.getConfigGuiDisplayName()}, new String[]{config.getName()})) {
                        maxWidth = Math.max(maxWidth, (int) (this.getStringWidth(config.getName()) * TranslatableLabel.TRANSLATION_SCALE));
                    }
                }
            }
        }
        return maxWidth;
    }

    @Inject(
            method = "reCreateListEntryWidgets",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/gui/widgets/WidgetListConfigOptionsBase;reCreateListEntryWidgets()V",
                    remap = false
            ),
            remap = false
    )
    private void adjustConfigAndOptionPanelWidth$tweakerkit(CallbackInfo ci) {
        if (!isTweakerKitConfigGui()) return;
        if (getFeatureValue(FeatureConfig.RIGHT_ALIGNED_PANE)) {
            Pair<Integer, Integer> result = TweakerKitConfigGui.adjustWidths(this.totalWidth, this.maxLabelWidth);
            this.maxLabelWidth = result.getFirst();
            this.configWidth = result.getSecond();
        }
    }
}
