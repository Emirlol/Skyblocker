package de.hysky.skyblocker.skyblock.crimson.kuudra

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.crimson.kuudra.Kuudra.KuudraPhase
import de.hysky.skyblocker.utils.ItemUtils.getItemId
import de.hysky.skyblocker.utils.Utils.isInKuudra
import de.hysky.skyblocker.utils.render.RenderHelper.displayInTitleContainerAndPlaySound
import de.hysky.skyblocker.utils.render.title.Title
import net.minecraft.client.MinecraftClient
import net.minecraft.item.BowItem
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.function.Supplier

object ArrowPoisonWarning {
	private val CONFIG = Supplier { SkyblockerConfigManager.get().crimsonIsle.kuudra }
	private const val THREE_SECONDS = 20 * 3
	private val NONE_TITLE = Title(Text.translatable("skyblocker.crimson.kuudra.noArrowPoison").formatted(Formatting.GREEN))
	private val LOW_TITLE = Title(Text.translatable("skyblocker.crimson.kuudra.lowArrowPoison").formatted(Formatting.GREEN))

	@JvmStatic
	fun tryWarn(newSlot: Int) {
		//Exclude skyblock menu
		if (isInKuudra && CONFIG.get().noArrowPoisonWarning && Kuudra.phase == KuudraPhase.DPS && newSlot != 8) {
			val client = MinecraftClient.getInstance()
			val inventory = client.player!!.inventory
			val heldItem = inventory.getStack(newSlot)

			if (heldItem.item is BowItem) {
				var hasToxicArrowPoison = false
				var arrowPoisonAmount = 0

				for (i in 0 until inventory.size()) {
					val stack = inventory.getStack(i)
					val itemId = getItemId(stack)

					if (itemId == "TOXIC_ARROW_POISON") {
						hasToxicArrowPoison = true
						arrowPoisonAmount += stack.count
					}
				}

				if (!hasToxicArrowPoison) {
					displayInTitleContainerAndPlaySound(NONE_TITLE, THREE_SECONDS)
				} else if (arrowPoisonAmount < CONFIG.get().arrowPoisonThreshold) {
					displayInTitleContainerAndPlaySound(LOW_TITLE, THREE_SECONDS)
				}
			}
		}
	}
}
