package de.hysky.skyblocker.skyblock.calculators

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.utils.Calculator.calculate
import de.hysky.skyblocker.utils.Constants
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.text.NumberFormat

object CalculatorCommand {
	private val CLIENT: MinecraftClient? = MinecraftClient.getInstance()
	private val FORMATTER: NumberFormat = NumberFormat.getInstance()

	fun init() {
		ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { obj: CommandDispatcher<FabricClientCommandSource?>?, dispatcher: CommandRegistryAccess? -> CalculatorCommand.calculate(dispatcher) })
	}

	private fun calculate(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandRegistryAccess) {
		dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
			.then(ClientCommandManager.literal("calculate")
				.then(ClientCommandManager.argument("equation", StringArgumentType.greedyString())
					.executes { context: CommandContext<FabricClientCommandSource?>? -> doCalculation(StringArgumentType.getString(context, "equation")) }
				)
			)
		)
	}

	private fun doCalculation(calculation: String): Int {
		val text = Constants.PREFIX.get()
		try {
			text.append(Text.literal(FORMATTER.format(calculate(calculation))).formatted(Formatting.GREEN))
		} catch (e: UnsupportedOperationException) {
			text.append(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.invalidEquation").formatted(Formatting.RED))
		}

		if (CLIENT?.player == null) {
			return 0
		}

		CLIENT.player!!.sendMessage(text, false)
		return Command.SINGLE_SUCCESS
	}
}
