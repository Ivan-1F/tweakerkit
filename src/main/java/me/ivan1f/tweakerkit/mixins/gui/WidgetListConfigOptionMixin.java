package me.ivan1f.tweakerkit.mixins.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerButton;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.*;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.util.StringUtils;
import me.ivan1f.tweakerkit.gui.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Objects;

@Mixin(WidgetConfigOption.class)
public abstract class WidgetListConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    @Shadow(remap = false)
    @Final
    protected IKeybindConfigGui host;

    @Shadow(remap = false)
    protected abstract void addKeybindResetButton(int x, int y, IKeybind keybind, ConfigButtonKeybind buttonHotkey);

    @Unique
    private boolean initialBoolean;

    @Shadow(remap = false)
    @Final
    protected GuiConfigsBase.ConfigOptionWrapper wrapper;

    @Mutable
    @Shadow(remap = false)
    @Final
    protected KeybindSettings initialKeybindSettings;

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

    /**
     * Stolen from malilib 1.18 v0.11.4
     * to make compact ConfigBooleanHotkeyed option panel works
     */
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void initInitialState(CallbackInfo ci) {
        if (isTweakerKitConfigGui() && this.wrapper.getType() == GuiConfigsBase.ConfigOptionWrapper.Type.CONFIG) {
            IConfigBase config = wrapper.getConfig();
            if (config instanceof ConfigBooleanHotkeyed) {
                this.initialBoolean = ((ConfigBooleanHotkeyed) config).getBooleanValue();
                this.initialStringValue = ((ConfigBooleanHotkeyed) config).getKeybind().getStringValue();
                this.initialKeybindSettings = ((ConfigBooleanHotkeyed) config).getKeybind().getSettings();
            }
        }
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
        if (!isTweakerKitConfigGui()) return;
        if (getFeatureValue(FeatureConfig.BETTER_CONFIG_PANE) && config instanceof IHotkey) {
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

    /**
     * Stolen from malilib 1.18 v0.11.4
     * to make compact ConfigBooleanHotkeyed option panel works
     */
    @Inject(
            method = "wasConfigModified",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/gui/GuiConfigsBase$ConfigOptionWrapper;getConfig()Lfi/dy/masa/malilib/config/IConfigBase;",
                    ordinal = 0,
                    remap = false
            ),
            cancellable = true,
            remap = false
    )
    private void specialJudgeCustomConfigBooleanHotkeyed(CallbackInfoReturnable<Boolean> cir) {
        if (!isTweakerKitConfigGui()) return;
        IConfigBase config = this.wrapper.getConfig();
        if (config instanceof ConfigBooleanHotkeyed) {
            ConfigBooleanHotkeyed booleanHotkey = (ConfigBooleanHotkeyed) config;
            IKeybind keybind = booleanHotkey.getKeybind();
            cir.setReturnValue(
                    this.initialBoolean != booleanHotkey.getBooleanValue() ||
                            !Objects.equals(this.initialStringValue, keybind.getStringValue()) ||
                            !Objects.equals(this.initialKeybindSettings, keybind.getSettings())
            );
        }
    }
}
