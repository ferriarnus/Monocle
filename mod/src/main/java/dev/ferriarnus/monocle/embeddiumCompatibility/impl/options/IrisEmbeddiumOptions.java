package dev.ferriarnus.monocle.embeddiumCompatibility.impl.options;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.pathways.colorspace.ColorSpace;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.embeddedt.embeddium.api.OptionGroupConstructionEvent;
import org.embeddedt.embeddium.api.options.control.ControlValueFormatter;
import org.embeddedt.embeddium.api.options.control.CyclingControl;
import org.embeddedt.embeddium.api.options.control.SliderControl;
import org.embeddedt.embeddium.api.options.storage.MinecraftOptionsStorage;
import org.embeddedt.embeddium.api.options.structure.*;

import java.io.IOException;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class IrisEmbeddiumOptions {

	private static final MinecraftOptionsStorage vanillaOpts = MinecraftOptionsStorage.INSTANCE;

	public static final ResourceLocation SHADOW_DISTANCE = ResourceLocation.fromNamespaceAndPath(Iris.MODID, "shadow_distance");
	public static final ResourceLocation COLOR_SPACE = ResourceLocation.fromNamespaceAndPath(Iris.MODID, "color_space");
	public static final ResourceLocation GRAPHICS_MODE = ResourceLocation.fromNamespaceAndPath(Iris.MODID, "graphics_mode");

	public static OptionImpl<Options, Integer> createMaxShadowDistanceSlider(MinecraftOptionsStorage vanillaOpts) {
		OptionImpl<Options, Integer> maxShadowDistanceSlider = OptionImpl.createBuilder(int.class, vanillaOpts)
			.setId(SHADOW_DISTANCE)
			.setName(Component.translatable("options.iris.shadowDistance"))
			.setTooltip(Component.translatable("options.iris.shadowDistance.sodium_tooltip"))
			.setControl(option -> new SliderControl(option, 0, 32, 1, translateVariableOrDisabled("options.chunks", "Disabled")))
			.setBinding((options, value) -> {
					IrisVideoSettings.shadowDistance = value;
					try {
						Iris.getIrisConfig().save();
					} catch (IOException e) {
						e.printStackTrace();
					}
				},
				options -> IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance))
			.setImpact(OptionImpact.HIGH)
			.setEnabledPredicate(IrisVideoSettings::isShadowDistanceSliderEnabled)
			.build();

		return maxShadowDistanceSlider;
	}

	public static OptionImpl<Options, ColorSpace> createColorSpaceButton(MinecraftOptionsStorage vanillaOpts) {
		OptionImpl<Options, ColorSpace> colorSpace = OptionImpl.createBuilder(ColorSpace.class, vanillaOpts)
			.setId(COLOR_SPACE)
			.setName(Component.translatable("options.iris.colorSpace"))
			.setTooltip(Component.translatable("options.iris.colorSpace.sodium_tooltip"))
			.setControl(option -> new CyclingControl<>(option, ColorSpace.class,
				new Component[]{Component.literal("sRGB"), Component.literal("DCI_P3"), Component.literal("Display P3"), Component.literal("REC2020"), Component.literal("Adobe RGB")}))
			.setBinding((options, value) -> {
					IrisVideoSettings.colorSpace = value;
					try {
						Iris.getIrisConfig().save();
					} catch (IOException e) {
						e.printStackTrace();
					}
				},
				options -> IrisVideoSettings.colorSpace)
			.setImpact(OptionImpact.LOW)
			.setEnabled(true)
			.build();


		return colorSpace;
	}

	static ControlValueFormatter translateVariableOrDisabled(String key, String disabled) {
		return (v) -> {
			return v == 0 ? Component.literal(disabled) : (Component.translatable(key, v));
		};
	}

	public static OptionImpl<Options, SupportedGraphicsMode> createLimitedVideoSettingsButton(MinecraftOptionsStorage vanillaOpts) {
		return OptionImpl.createBuilder(SupportedGraphicsMode.class, vanillaOpts)
			.setId(GRAPHICS_MODE)
			.setName(Component.translatable("options.graphics"))
			// TODO: State that Fabulous Graphics is incompatible with Shader Packs in the tooltip
			.setTooltip(Component.translatable("sodium.options.graphics_quality.tooltip"))
			.setControl(option -> new CyclingControl<>(option, SupportedGraphicsMode.class,
				new Component[]{Component.translatable("options.graphics.fast"), Component.translatable("options.graphics.fancy")}))
			.setBinding(
				(opts, value) -> opts.graphicsMode().set(value.toVanilla()),
				opts -> SupportedGraphicsMode.fromVanilla(opts.graphicsMode().get()))
			.setImpact(OptionImpact.HIGH)
			.setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
			.build();
	}

	@SubscribeEvent
	public static void addOptions(OptionGroupConstructionEvent event) {
		if (event.getId().matches(StandardOptions.Group.RENDERING)) {
			event.getOptions().add(createMaxShadowDistanceSlider(vanillaOpts));
		} else if (event.getId().matches(StandardOptions.Group.DETAILS)) {
			event.getOptions().add(createColorSpaceButton(vanillaOpts));
		} else if (event.getId().matches(StandardOptions.Group.GRAPHICS)) {
			int i = -1;
			for (var option : event.getOptions()) {
				if (option.getId().matches(StandardOptions.Option.GRAPHICS_MODE)) {
					i = event.getOptions().indexOf(option);
					break;
				}
			}
			if (i > -1) {
				event.getOptions().set(i, createLimitedVideoSettingsButton(vanillaOpts));
			}
		}
	}
}
