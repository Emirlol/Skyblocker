package de.hysky.skyblocker.config

import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.utils.FileUtils.normalizePath
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.controller.*
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import java.nio.file.Path
import java.util.function.Function

object ConfigUtils {
	@JvmField
	val FORMATTING_FORMATTER: ValueFormatter<Formatting> = ValueFormatter { formatting: Formatting -> Text.literal(StringUtils.capitalize(formatting.getName().replace("_".toRegex(), " "))) }
	@JvmField
	val FLOAT_TWO_FORMATTER: ValueFormatter<Float> = ValueFormatter { value: Float? -> Text.literal(String.format("%,.2f", value).replace("[\u00a0\u202F]".toRegex(), " ")) }
	private val IMAGE_DIRECTORY: Path = ImageRepoLoader.REPO_DIRECTORY.resolve("Skyblocker-Assets-images")

	@JvmStatic
	fun createBooleanController(opt: Option<Boolean?>?): BooleanControllerBuilder {
		return BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true)
	}

	@JvmStatic
	fun <E : Enum<E>?> createEnumCyclingListController(opt: Option<E>): EnumControllerBuilder<E> {
		return EnumControllerBuilder.create(opt).enumClass(opt.binding().defaultValue().javaClass as Class<E>?)
	}

	/**
	 * Creates a factory for [EnumDropdownControllerBuilder]s with the given function for converting enum constants to texts.
	 * Use this if a custom formatter function for an enum is needed.
	 * Use it like this:
	 * <pre>`Option.<MyEnum>createBuilder().controller(ConfigUtils.getEnumDropdownControllerFactory(MY_CUSTOM_ENUM_TO_TEXT_FUNCTION))`</pre>
	 *
	 * @param formatter The function used to convert enum constants to texts used for display, suggestion, and validation
	 * @param <E>       the enum type
	 * @return a factory for [EnumDropdownControllerBuilder]s
	</E> */
	@JvmStatic
	fun <E : Enum<E>?> getEnumDropdownControllerFactory(formatter: ValueFormatter<E>?): Function<Option<E>, ControllerBuilder<E>> {
		return Function { opt: Option<E>? -> EnumDropdownControllerBuilder.create(opt).formatValue(formatter) }
	}

	/**
	 * Creates an [OptionDescription] with an image and text.
	 */
	@SafeVarargs
	fun withImage(imagePath: Path?, vararg texts: Text?): OptionDescription {
		return OptionDescription.createBuilder()
			.text(*if (ArrayUtils.isNotEmpty(texts)) texts else arrayOf())
			.image(IMAGE_DIRECTORY.resolve(imagePath), Identifier(SkyblockerMod.NAMESPACE, "config_image_" + normalizePath(imagePath!!)))
			.build()
	}
}
