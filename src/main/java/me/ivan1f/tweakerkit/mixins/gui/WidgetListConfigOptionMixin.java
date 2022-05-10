package me.ivan1f.tweakerkit.mixins.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerButton;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.*;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.util.StringUtils;
import me.ivan1f.tweakerkit.gui.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WidgetConfigOption.class)
public abstract class WidgetListConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    @Shadow(remap = false) @Final protected IKeybindConfigGui host;

    @Shadow(remap = false) protected abstract void addKeybindResetButton(int x, int y, IKeybind keybind, ConfigButtonKeybind buttonHotkey);

    public WidgetListConfigOptionMixin(int x, int y, int width, int height, WidgetListConfigOptionsBase<?, ?> parent, GuiConfigsBase.ConfigOptionWrapper entry, int listIndex) {
        super(x, y, width, height, parent, entry, listIndex);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isTweakerKitConfigGui() {
        return this.parent instanceof WidgetListConfigOptions && ((WidgetListConfigOptionsAccessor) this.parent).getParent() instanceof TweakerKitConfigGui;
    }

    private <T> T getFeatureValue(FeatureConfig.Key<T> key) {
        return ((TweakerKitConfigGui) ((WidgetListConfigOptionsAccessor) this.parent).getParent()).getFeatureValue(key);
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
        if (!isTweakerKitConfigGui()) return;
        if (getFeatureValue(FeatureConfig.TRANSLATABLE_LABEL)) {
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

            TranslatableLabel label = new TranslatableLabel(x, y, width, height, textColor, lines, new String[]{config.getName()});
            this.addWidget(label);
        }
    }

    @Inject(
            method = "addHotkeyConfigElements",
            at = @At(value = "HEAD"),
            remap = false,
            cancellable = true
    )
    private void tweakerMoreCustomConfigGui(int x, int y, int configWidth, String configName, IHotkey config, CallbackInfo ci) {
        if (!isTweakerKitConfigGui()) return;
        if (getFeatureValue(FeatureConfig.BETTER_CONFIG_PANE)) {
            if ((config).getKeybind() instanceof KeybindMulti)
            {
                this.addButtonAndHotkeyWidgets$tweakerkit(x, y, configWidth, config);
                ci.cancel();
            }
        }
    }

    private void addButtonAndHotkeyWidgets$tweakerkit(int x, int y, int configWidth, IHotkey config) {
        IKeybind keybind = config.getKeybind();

        int triggerBtnWidth = (configWidth - 24) / 2;
        ButtonGeneric triggerButton = new ButtonGeneric(
                x, y, triggerBtnWidth, 20,
                StringUtils.translate("tweakerkit.gui.trigger_button.text"),
                StringUtils.translate("tweakerkit.gui.trigger_button.hover", config.getName())
        );
        this.addButton(triggerButton, (button, mouseButton) -> {
            IHotkeyCallback callback = ((KeybindMultiAccessor) keybind).getCallback();
            KeyAction activateOn = keybind.getSettings().getActivateOn();
            if (activateOn == KeyAction.BOTH || activateOn == KeyAction.PRESS) {
                callback.onKeyAction(KeyAction.PRESS, keybind);
            }
            if (activateOn == KeyAction.BOTH || activateOn == KeyAction.RELEASE) {
                callback.onKeyAction(KeyAction.RELEASE, keybind);
            }
        });

        x += triggerBtnWidth + 2;
        configWidth -= triggerBtnWidth + 2 + 22;

        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
        x += configWidth + 2;

        this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, config.getName(), this.parent, this.host.getDialogHandler()));
        x += 24;

        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addKeybindResetButton(x, y, keybind, keybindButton);
    }
}
