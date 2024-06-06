package de.hysky.skyblocker.skyblock.calculators

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Calculator.calculate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.text.NumberFormat
import kotlin.math.round

object SignCalculator {
	private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
	private val FORMATTER: NumberFormat = NumberFormat.getInstance()

	private var lastInput: String? = null
	private var output: Double? = null

	fun renderCalculator(context: DrawContext, message: String, renderX: Int, renderY: Int) {
		if (SkyblockerConfigManager.config.uiAndVisuals.inputCalculator.requiresEquals && !message.startsWith("=")) {
			output = null
			lastInput = message
			return
		}

		var message = message
		if (message.startsWith("=")) {
			message = message.substring(1)
		}
		//only update output if new input
		if (message != lastInput) {
			output = try {
				calculate(message)
			} catch (e: Exception) {
				null
			}
		}

		render(context, message, renderX, renderY)

		lastInput = message
	}

	fun getNewValue(isPrice: Boolean) =
		if (output == null) lastInput //if mode is not activated or just invalid equation return what the user typed in
		else if (isPrice) output.toString() //price can except decimals and exponents
		else round(output!!).toString() //amounts want an integer number so round

	private fun render(context: DrawContext, input: String, renderX: Int, renderY: Int) {
		val text = if (output == null) {
			Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.invalidEquation").formatted(Formatting.RED)
		} else {
			Text.literal(input + " = " + FORMATTER.format(output)).formatted(Formatting.GREEN)
		}

		context.drawCenteredTextWithShadow(CLIENT.textRenderer, text, renderX, renderY, -0x1)
	}
}
