package de.hysky.skyblocker.skyblock.calculators

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.utils.Calculator.calculate
import de.hysky.skyblocker.utils.Constants
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.text.NumberFormat

object CalculatorCommand {
	private val FORMATTER: NumberFormat = NumberFormat.getInstance()

	fun init() {
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
				.then(ClientCommandManager.literal("calculate")
					.then(ClientCommandManager.argument("equation", StringArgumentType.greedyString())
						.executes { context -> doCalculation(StringArgumentType.getString(context, "equation")) }
					)
				)
			)
		}
	}

	private fun doCalculation(calculation: String): Int {
		val text = Constants.PREFIX
		try {
			text.append(Text.literal(FORMATTER.format(calculate(calculation))).formatted(Formatting.GREEN))
		} catch (e: UnsupportedOperationException) {
			text.append(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.invalidEquation").formatted(Formatting.RED))
		}

		MinecraftClient.getInstance().player?.sendMessage(text, false) ?: return 0
		return Command.SINGLE_SUCCESS
	}
}
