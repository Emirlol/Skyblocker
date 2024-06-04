package de.hysky.skyblocker.config

import com.google.gson.FieldNamingPolicy
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.config.categories.GeneralCategory
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.dsl.YetAnotherConfigLib
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object SkyblockerConfigManager {
	const val CONFIG_VERSION = 3
	private val CONFIG_FILE = FabricLoader.getInstance().configDir.resolve("skyblocker.json")
	private val HANDLER = ConfigClassHandler.createBuilder(SkyblockerConfig::class.java)
		.serializer {
			GsonConfigSerializerBuilder.create(it)
				.setPath(CONFIG_FILE)
				.setJson5(false)
				.appendGsonBuilder { builder ->
					builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
						.registerTypeHierarchyAdapter(Identifier::class.java, Identifier.Serializer())
				}.build()
		}.build()

	init {
		HANDLER.load()
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess -> dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(optionsLiteral("config")).then(optionsLiteral("options"))) }
		ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
			if (screen is GenericContainerScreen && screen.title.string == "SkyBlock Menu") {
				Screens.getButtons(screen).add(
					ButtonWidget.builder(Text.literal("\uD83D\uDD27")) { client.setScreen(createGUI(screen)) }
						.dimensions((screen as HandledScreenAccessor).getX() + (screen as HandledScreenAccessor).getBackgroundWidth() - 16, (screen as HandledScreenAccessor).getY() + 4, 12, 12)
						.tooltip(Tooltip.of(Text.translatable("skyblocker.config.title")))
						.build())
			}
		}
	}

	val config get() = HANDLER.instance()

	val defaults get() = HANDLER.defaults()

	fun save() = HANDLER.save()

	fun createGUI(parent: Screen) = YetAnotherConfigLib(SkyblockerMod.NAMESPACE) {
		title(Text.translatable("skyblocker.config.title"))
		category(GeneralCategory::class.simpleName!!) {

		}
	}
}