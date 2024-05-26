package de.hysky.skyblocker.skyblock

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.skyblock.item.CustomArmorAnimatedDyes
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import org.apache.commons.lang3.math.NumberUtils
import java.util.regex.Pattern

object CompactDamage {
	private val DAMAGE_PATTERN: Pattern = Pattern.compile("[✧✯]?[\\d,]+[✧✯]?❤?")
	fun compactDamage(entity: ArmorStandEntity) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.compactDamage.enabled) return
		if (!entity.isInvisible || !entity.hasCustomName() || !entity.isCustomNameVisible) return
		val customName = entity.customName!!
		val customNameStringified = customName.string
		if (!DAMAGE_PATTERN.matcher(customNameStringified).matches()) return
		val siblings = customName.siblings
		if (siblings.isEmpty()) return

		val prettierCustomName: MutableText
		if (siblings.size == 1) { //Non-crit damage
			val text = siblings.first()
			val dmg = text.string.replace(",", "")
			if (!NumberUtils.isParsable(dmg)) return  //Sanity check

			val prettifiedDmg = prettifyDamageNumber(dmg.toLong())
			val color = if (text.style.color != null) {
				if (text.style.color === TextColor.fromFormatting(Formatting.GRAY)) {
					SkyblockerConfigManager.get().uiAndVisuals.compactDamage.normalDamageColor.rgb and 0x00FFFFFF
				} else text.style.color!!.rgb
			} else SkyblockerConfigManager.get().uiAndVisuals.compactDamage.normalDamageColor.rgb and 0x00FFFFFF
			prettierCustomName = Text.empty().append(Text.literal(prettifiedDmg).setStyle(customName.style).withColor(color))
		} else { //Crit damage
			val wasDoubled = customNameStringified.contains("❤") //Ring of love ability adds a heart to the end of the damage string
			val entriesToRemove = if (wasDoubled) 2 else 1

			val dmg = siblings.subList(1, siblings.size - entriesToRemove) //First and last sibling are the crit symbols and maybe heart
				.stream()
				.map { obj: Text -> obj.string }
				.reduce("") { obj: String, str: String -> obj + str } //Concatenate all the siblings to get the dmg number
				.replace(",", "")

			if (!NumberUtils.isParsable(dmg)) return  //Sanity check

			val dmgSymbol = if (customNameStringified[0] != '✯') "✧" else "✯" //Mega Crit ability from the Overload enchantment
			val prettifiedDmg = dmgSymbol + prettifyDamageNumber(dmg.toLong()) + dmgSymbol
			val length = prettifiedDmg.length
			prettierCustomName = Text.empty()
			for (i in 0 until length) {
				prettierCustomName.append(
					Text.literal(prettifiedDmg.substring(i, i + 1)).withColor(
						CustomArmorAnimatedDyes.interpolate(
							SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientStart.rgb and 0x00FFFFFF,
							SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientEnd.rgb and 0x00FFFFFF,
							i / (length - 1.0)
						)
					)
				)
			}

			if (wasDoubled) prettierCustomName.append(Text.literal("❤").formatted(Formatting.LIGHT_PURPLE))

			prettierCustomName.setStyle(customName.style)
		}

		entity.customName = prettierCustomName
	}

	private fun prettifyDamageNumber(damage: Long): String {
		if (damage < 1000) return damage.toString()
		if (damage < 1000000) return format(damage / 1000.0) + "k"
		if (damage < 1000000000) return format(damage / 1000000.0) + "M"
		if (damage < 1000000000000L) return format(damage / 1000000000.0) + "B"
		return format(damage / 1000000000000.0) + "T" //This will probably never be reached
	}

	private fun format(number: Double) = "%.${SkyblockerConfigManager.get().uiAndVisuals.compactDamage.precision}f".format(number)
}
