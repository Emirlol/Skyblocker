package de.hysky.skyblocker.skyblock.item

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.debug.Debug
import de.hysky.skyblocker.events.SkyblockEvents
import de.hysky.skyblocker.events.SkyblockEvents.SkyblockJoin
import de.hysky.skyblocker.skyblock.item.CustomArmorTrims.ArmorTrimId
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.ItemUtils.getItemUuid
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import dev.isxander.yacl3.config.v2.api.SerialEntry
import it.unimi.dsi.fastutil.Pair
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.item.ArmorItem
import net.minecraft.item.trim.ArmorTrim
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CustomArmorTrims {
	private val LOGGER: Logger = LoggerFactory.getLogger(CustomArmorTrims::class.java)
	@JvmField
	val TRIMS_CACHE: Object2ObjectOpenHashMap<ArmorTrimId, ArmorTrim?> = Object2ObjectOpenHashMap()
	private var trimsInitialized = false

	fun init() {
		SkyblockEvents.JOIN.register(SkyblockJoin { obj: CustomArmorTrims? -> initializeTrimCache() })
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> registerCommand(dispatcher) })
	}

	private fun initializeTrimCache() {
		val player = MinecraftClient.getInstance().player
		val loader = FabricLoader.getInstance()
		if (trimsInitialized || (player == null && !Debug.debugEnabled())) {
			return
		}
		try {
			TRIMS_CACHE.clear()
			val wrapperLookup = getWrapperLookup(loader, player)
			for (material in wrapperLookup.getWrapperOrThrow(RegistryKeys.TRIM_MATERIAL).streamEntries().toList()) {
				for (pattern in wrapperLookup.getWrapperOrThrow(RegistryKeys.TRIM_PATTERN).streamEntries().toList()) {
					val trim = ArmorTrim(material, pattern)

					TRIMS_CACHE[ArmorTrimId(material.registryKey().value, pattern.registryKey().value)] = trim
				}
			}

			LOGGER.info("[Skyblocker] Successfully cached all armor trims!")
			trimsInitialized = true
		} catch (e: Exception) {
			LOGGER.error("[Skyblocker] Encountered an exception while caching armor trims", e)
		}
	}

	private fun getWrapperLookup(loader: FabricLoader, player: ClientPlayerEntity?): WrapperLookup {
		return if (!Debug.debugEnabled()) player!!.networkHandler.registryManager else BuiltinRegistries.createWrapperLookup()
	}

	private fun registerCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
			.then(ClientCommandManager.literal("custom")
				.then(ClientCommandManager.literal("armorTrim")
					.executes { context: CommandContext<FabricClientCommandSource> -> customizeTrim(context.source, null, null) }
					.then(ClientCommandManager.argument("material", IdentifierArgumentType.identifier())
						.suggests(getIdSuggestionProvider(RegistryKeys.TRIM_MATERIAL))
						.executes { context: CommandContext<FabricClientCommandSource> -> customizeTrim(context.source, context.getArgument("material", Identifier::class.java), null) }
						.then(ClientCommandManager.argument("pattern", IdentifierArgumentType.identifier())
							.suggests(getIdSuggestionProvider(RegistryKeys.TRIM_PATTERN))
							.executes { context: CommandContext<FabricClientCommandSource> -> customizeTrim(context.source, context.getArgument("material", Identifier::class.java), context.getArgument("pattern", Identifier::class.java)) })
					)
				)
			)
		)
	}

	private fun getIdSuggestionProvider(registryKey: RegistryKey<out Registry<*>?>): SuggestionProvider<FabricClientCommandSource> {
		return SuggestionProvider { context: CommandContext<FabricClientCommandSource>, builder: SuggestionsBuilder? -> context.source.listIdSuggestions(registryKey, CommandSource.SuggestedIdType.ELEMENTS, builder, context) }
	}

	private fun customizeTrim(source: FabricClientCommandSource, material: Identifier?, pattern: Identifier?): Int {
		val heldItem = source.player.mainHandStack

		if (isOnSkyblock && heldItem != null) {
			if (heldItem.item is ArmorItem) {
				val itemUuid = getItemUuid(heldItem)

				if (!itemUuid.isEmpty()) {
					val customArmorTrims = SkyblockerConfigManager.get().general.customArmorTrims

					if (material == null && pattern == null) {
						if (customArmorTrims.containsKey(itemUuid)) {
							customArmorTrims.remove(itemUuid)
							SkyblockerConfigManager.save()
							source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.removed")))
						} else {
							source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.neverHad")))
						}
					} else {
						// Ensure that the material & trim are valid
						val trimId = ArmorTrimId(material, pattern)
						if (TRIMS_CACHE[trimId] == null) {
							source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.invalidMaterialOrPattern")))

							return Command.SINGLE_SUCCESS
						}

						customArmorTrims[itemUuid] = trimId
						SkyblockerConfigManager.save()
						source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.added")))
					}
				} else {
					source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.noItemUuid")))
				}
			} else {
				source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.notAnArmorPiece")))
				return Command.SINGLE_SUCCESS
			}
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.unableToSetTrim")))
		}

		return Command.SINGLE_SUCCESS
	}

	@JvmRecord
	data class ArmorTrimId(@field:SerialEntry @param:SerialEntry val material: Identifier?, @field:SerialEntry @param:SerialEntry val pattern: Identifier?) : Pair<Identifier?, Identifier?> {
		override fun left(): Identifier? {
			return material
		}

		override fun right(): Identifier? {
			return pattern
		}

		companion object {
			val CODEC: Codec<ArmorTrimId?> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<ArmorTrimId?> ->
				instance.group(
					Identifier.CODEC.fieldOf("material").forGetter(ArmorTrimId::material),
					Identifier.CODEC.fieldOf("pattern").forGetter(ArmorTrimId::pattern)
				)
					.apply(instance) { material: Identifier?, pattern: Identifier? -> ArmorTrimId(material, pattern) }
			}
		}
	}
}
