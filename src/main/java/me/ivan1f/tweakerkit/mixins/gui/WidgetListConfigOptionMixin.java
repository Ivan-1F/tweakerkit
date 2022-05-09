package me.ivan1f.tweakerkit.mixins.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import me.ivan1f.tweakerkit.gui.TranslatableLabel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WidgetConfigOption.class)
public abstract class WidgetListConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    public WidgetListConfigOptionMixin(int x, int y, int width, int height, WidgetListConfigOptionsBase<?, ?> parent, GuiConfigsBase.ConfigOptionWrapper entry, int listIndex) {
        super(x, y, width, height, parent, entry, listIndex);
    }

    @ModifyArgs(
            method = "addConfigOption",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;addLabel(IIIII[Ljava/lang/String;)V",
                    remap = false
            ),
            remap = false
    )
    private void useTranslatableLabel(Args args, int x_, int y_, float zLevel, int labelWidth, int configWidth, IConfigBase config) {
        int x = args.get(0);
        int y = args.get(1);
        int width = args.get(2);
        int height = args.get(3);
        int textColor = args.get(4);
        String[] lines = args.get(5);

        if (lines == null || lines.length != 1) {
            return;
        }

        args.set(5, null);  // cancel original call

        TranslatableLabel label = new TranslatableLabel(x, y, width, height, textColor, lines, config.getName());
        this.addWidget(label);
    }
}
