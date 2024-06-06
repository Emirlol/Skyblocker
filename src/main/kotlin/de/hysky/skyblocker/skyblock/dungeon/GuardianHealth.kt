package de.hysky.skyblocker.skyblock.dungeon

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.Utils.isInDungeons
import de.hysky.skyblocker.utils.render.RenderHelper.renderText
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.GuardianEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.Box
import java.util.regex.Pattern

object GuardianHealth {
	private val bossRoom = Box(34.0, 65.0, -32.0, -32.0, 100.0, 36.0)
	private val guardianRegex: Pattern = Pattern.compile("^(.*?) Guardian (.*?)([A-Za-z])❤$")
	private val professorRegex: Pattern = Pattern.compile("^﴾ The Professor (.*?)([A-Za-z])❤ ﴿$")
	private var inBoss = false

	fun init() {
		ClientReceiveMessageEvents.GAME.register { message, _ -> onChatMessage(message) }
		ClientPlayConnectionEvents.JOIN.register { _, _, _ -> reset() }
		WorldRenderEvents.AFTER_ENTITIES.register(::onWorldRender)
	}

	private fun onWorldRender(context: WorldRenderContext) {
		if (!SkyblockerConfigManager.config.dungeons.theProfessor.floor3GuardianHealthDisplay) return

		val client = MinecraftClient.getInstance()

		if (isInDungeons && inBoss && client.player != null && client.world != null) {
			val guardians = client.world!!.getEntitiesByClass(GuardianEntity::class.java, bossRoom) { true }

			for (guardian in guardians) {
				val armorStands = client.world!!.getEntitiesByType(EntityType.ARMOR_STAND, guardian.boundingBox.expand(0.0, 1.0, 0.0), ::isGuardianName)

				for (armorStand in armorStands) {
					val display = armorStand.displayName!!.string
					val professor = display.contains("The Professor")
					val matcher = if (professor) professorRegex.matcher(display) else guardianRegex.matcher(display)
					matcher.matches() // name is validated in isGuardianName

					val health = matcher.group(if (professor) 1 else 2)
					val quantity = matcher.group(if (professor) 2 else 3)

					val distance = context.camera().pos.distanceTo(guardian.pos)

					renderText(context, Text.literal(health + quantity).formatted(Formatting.GREEN), guardian.pos, (1 + (distance / 10)).toFloat(), true)
				}
			}
		}
	}

	private fun reset() {
		inBoss = false
	}

	private fun onChatMessage(text: Text) {
		if (isInDungeons && SkyblockerConfigManager.config.dungeons.theProfessor.floor3GuardianHealthDisplay && !inBoss) {
			val unformatted = Formatting.strip(text.string)

			inBoss = unformatted == "[BOSS] The Professor: I was burdened with terrible news recently..."
		}
	}

	private fun isGuardianName(entity: ArmorStandEntity) = entity.displayName!!.string.let {
		if (it.contains("The Professor")) professorRegex.matcher(it).matches()
		else guardianRegex.matcher(it).matches()
	}
}

