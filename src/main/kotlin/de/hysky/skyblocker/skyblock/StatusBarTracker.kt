package de.hysky.skyblocker.skyblock

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInTheRift
import de.hysky.skyblocker.utils.Utils.isOnSkyblock
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.AllowGame
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.ModifyGame
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min

class StatusBarTracker {
	var health: Resource = Resource(100, 100, 0)
		private set
	var mana: Resource = Resource(100, 100, 0)
		private set
	var defense: Int = 0
		private set

	fun init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(AllowGame { text: Text, overlay: Boolean -> this.allowOverlayMessage(text, overlay) })
		ClientReceiveMessageEvents.MODIFY_GAME.register(ModifyGame { text: Text, overlay: Boolean -> this.onOverlayMessage(text, overlay) })
	}

	private fun parseInt(m: Matcher, group: Int): Int {
		return m.group(group).replace(",", "").toInt()
	}

	private fun updateMana(m: Matcher) {
		val value = parseInt(m, 1)
		val max = parseInt(m, 3)
		val overflow = if (m.group(5) == null) 0 else parseInt(m, 5)
		this.mana = Resource(value, max, overflow)
	}

	private fun updateHealth(m: Matcher) {
		var value = parseInt(m, 1)
		val max = parseInt(m, 3)
		var overflow = max(0.0, (value - max).toDouble()).toInt()
		if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
			val player = MinecraftClient.getInstance().player
			value = (player!!.health * max / player.maxHealth).toInt()
			overflow = (player.absorptionAmount * max / player.maxHealth).toInt()
		}
		this.health = Resource(min(value.toDouble(), max.toDouble()).toInt(), max, min(overflow.toDouble(), max.toDouble()).toInt())
	}

	private fun reset(str: String, m: Matcher): String {
		var str = str
		str = str.substring(m.end())
		m.reset(str)
		return str
	}

	private fun allowOverlayMessage(text: Text, overlay: Boolean): Boolean {
		onOverlayMessage(text, overlay)
		return true
	}

	private fun onOverlayMessage(text: Text, overlay: Boolean): Text {
		if (!overlay || !isOnSkyblock || !SkyblockerConfigManager.get().uiAndVisuals.bars.enableBars || isInTheRift) {
			return text
		}
		return Text.of(update(text.string, SkyblockerConfigManager.get().chat.hideMana))
	}

	fun update(actionBar: String, filterManaUse: Boolean): String? {
		var actionBar = actionBar
		val sb = StringBuilder()
		val matcher = STATUS_HEALTH.matcher(actionBar)
		if (!matcher.lookingAt()) return actionBar
		updateHealth(matcher)
		if (matcher.group(5) != null) {
			sb.append("§c❤")
			sb.append(matcher.group(5))
		}
		actionBar = reset(actionBar, matcher)
		if (matcher.usePattern(MANA_STATUS).lookingAt()) {
			defense = 0
			updateMana(matcher)
			actionBar = reset(actionBar, matcher)
		} else {
			if (matcher.usePattern(DEFENSE_STATUS).lookingAt()) {
				defense = parseInt(matcher, 1)
				actionBar = reset(actionBar, matcher)
			} else if (filterManaUse && matcher.usePattern(MANA_USE).lookingAt()) {
				actionBar = reset(actionBar, matcher)
			}
			if (matcher.usePattern(MANA_STATUS).find()) {
				updateMana(matcher)
				matcher.appendReplacement(sb, "")
			}
		}
		matcher.appendTail(sb)
		val res = sb.toString().trim { it <= ' ' }
		return if (res.isEmpty()) null else res
	}

	@JvmRecord
	data class Resource(@JvmField val value: Int, @JvmField val max: Int, @JvmField val overflow: Int)
	companion object {
		private val STATUS_HEALTH: Pattern = Pattern.compile("§[6c](\\d+(,\\d\\d\\d)*)/(\\d+(,\\d\\d\\d)*)❤(?:(\\+§c(\\d+(,\\d\\d\\d)*). *)| *)")
		private val DEFENSE_STATUS: Pattern = Pattern.compile("§a(\\d+(,\\d\\d\\d)*)§a❈ Defense *")
		private val MANA_USE: Pattern = Pattern.compile("§b-(\\d+(,\\d\\d\\d)*) Mana \\(§\\S+(?:\\s\\S+)* *")
		private val MANA_STATUS: Pattern = Pattern.compile("§b(\\d+(,\\d\\d\\d)*)/(\\d+(,\\d\\d\\d)*)✎ (?:Mana|§3(\\d+(,\\d\\d\\d)*)ʬ) *")
	}
}
