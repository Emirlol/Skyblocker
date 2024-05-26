package de.hysky.skyblocker.skyblock.calculators

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Calculator.calculate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.text.NumberFormat

object SignCalculator {
	private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
	private val FORMATTER: NumberFormat = NumberFormat.getInstance()

	private var lastInput: String? = null
	private var output = 0.0

	@JvmStatic
	fun renderCalculator(context: DrawContext, message: String, renderX: Int, renderY: Int) {
		var message = message
		if (SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.requiresEquals && !message.startsWith("=")) {
			output = -1.0
			lastInput = message
			return
		}
		if (message.startsWith("=")) {
			message = message.substring(1)
		}
		//only update output if new input
		if (message != lastInput) { //
			try {
				output = calculate(message)
			} catch (e: Exception) {
				output = -1.0
			}
		}

		render(context, message, renderX, renderY)

		lastInput = message
	}

	@JvmStatic
	fun getNewValue(isPrice: Boolean): String? {
		if (output == -1.0) {
			//if mode is not activated or just invalid equation return what the user typed in
			return lastInput
		}

		//price can except decimals and exponents
		if (isPrice) {
			return output.toString()
		}
		//amounts want an integer number so round
		return Math.round(output).toString()
	}

	private fun render(context: DrawContext, input: String, renderX: Int, renderY: Int) {
		val text: Text = if (output == -1.0) {
			Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.invalidEquation").formatted(Formatting.RED)
		} else {
			Text.literal(input + " = " + FORMATTER.format(output)).formatted(Formatting.GREEN)
		}

		context.drawCenteredTextWithShadow(CLIENT.textRenderer, text, renderX, renderY, -0x1)
	}
}
