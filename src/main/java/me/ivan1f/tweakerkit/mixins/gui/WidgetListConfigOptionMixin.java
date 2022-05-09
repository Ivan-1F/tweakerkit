package me.ivan1f.tweakerkit.mixins.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerButton;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.util.StringUtils;
import me.ivan1f.tweakerkit.gui.HotkeyedBooleanResetListener;
import me.ivan1f.tweakerkit.gui.TranslatableLabel;
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
    @Shadow @Final protected IKeybindConfigGui host;

    @Shadow protected abstract void addKeybindResetButton(int x, int y, IKeybind keybind, ConfigButtonKeybind buttonHotkey);

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

    @Inject(
            method = "addConfigOption",
            at = @At(
                    value = "FIELD",
                    target = "Lfi/dy/masa/malilib/config/ConfigType;BOOLEAN:Lfi/dy/masa/malilib/config/ConfigType;",
                    remap = false
            ),
            remap = false,
            cancellable = true
    )
    private void tweakerKitCustomConfigGui(int x, int y, float zLevel, int labelWidth, int configWidth, IConfigBase config, CallbackInfo ci) {
        if (config instanceof IHotkey) {
            boolean modified = true;
            if (config instanceof IHotkeyTogglable) {
                this.addBooleanAndHotkeyWidgets$tweakerkit(x, y, configWidth, (IHotkeyTogglable) config);
            } else if (((IHotkey) config).getKeybind() instanceof KeybindMulti) {
                this.addButtonAndHotkeyWidgets$tweakerkit(x, y, configWidth, (IHotkey) config);
            } else {
                modified = false;
            }
            if (modified) {
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

    private void addBooleanAndHotkeyWidgets$tweakerkit(int x, int y, int configWidth, IHotkeyTogglable config) {
        IKeybind keybind = config.getKeybind();

        int booleanBtnWidth = (configWidth - 24) / 2;
        ConfigButtonBoolean booleanButton = new ConfigButtonBoolean(x, y, booleanBtnWidth, 20, config);
        x += booleanBtnWidth + 2;
        configWidth -= booleanBtnWidth + 2 + 22;

        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
        x += configWidth + 2;

        this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, config.getName(), this.parent, this.host.getDialogHandler()));
        x += 24;

        ButtonGeneric resetButton = this.createResetButton(x, y, config);

        ConfigOptionChangeListenerButton booleanChangeListener = new ConfigOptionChangeListenerButton(config, resetButton, null);
        HotkeyedBooleanResetListener resetListener = new HotkeyedBooleanResetListener(config, booleanButton, keybindButton, resetButton, this.host);

        this.host.addKeybindChangeListener(resetListener);

        this.addButton(booleanButton, booleanChangeListener);
        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addButton(resetButton, resetListener);
    }

}
