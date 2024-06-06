package de.hysky.skyblocker.skyblock.dungeon.partyfinder

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.mixins.accessors.SkullBlockEntityAccessor
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.PlayerSkinDrawer
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ProfileComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*
import java.util.regex.Pattern
import kotlin.math.min

open class PartyEntry(tooltips: List<Text>, protected val screen: PartyFinderScreen?, protected val slotID: Int) : ElementListWidget.Entry<PartyEntry?>() {
	var partyLeader: Player? = null
	var floor: String = "???"
	var dungeon: String = "???"
	var note: String = ""
	var floorSkullProperties: PropertyMap = PropertyMap()
	var partyLeaderSkin: Identifier = DefaultSkinHelper.getTexture()
	var partyMembers: Array<Player?> = arrayOfNulls(4)

	var minClassLevel: Int = -1
	var minCatacombsLevel: Int = -1

	var isLocked: Boolean = false
	var lockReason: Text = Text.empty()


	init {
		Arrays.fill(partyMembers, null)
		if (tooltips.isEmpty()) return

		//System.out.println(tooltips);
		val client = MinecraftClient.getInstance()
		val title = tooltips.first
		val partyHost = title.string.split("'s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

		var membersIndex = -1
		for (i in tooltips.indices) {
			val text = tooltips[i]
			val tooltipText = checkNotNull(Formatting.strip(text.string))
			val lowerCase = tooltipText.lowercase(Locale.getDefault())
			//System.out.println("TOOLTIP"+i);
			//System.out.println(text.getSiblings());
			if (lowerCase.contains("members:") && membersIndex == -1) {
				membersIndex = i + 1
			} else if (lowerCase.contains("class level")) {
				val matcher = Pattern.compile("\\d+$").matcher(lowerCase)
				if (matcher.find()) minClassLevel = matcher.group().toInt()
			} else if (lowerCase.contains("dungeon level")) {
				val matcher = Pattern.compile("\\d+$").matcher(lowerCase)
				if (matcher.find()) minCatacombsLevel = matcher.group().toInt()
			} else if (lowerCase.contains("floor:")) {
				floor = tooltipText.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }
				if (dungeon == "???") continue
				if (PartyFinderScreen.Companion.floorIconsMaster == null || PartyFinderScreen.Companion.floorIconsNormal == null) continue
				floorSkullProperties = if (dungeon.contains("Master Mode")) {
					try {
						PartyFinderScreen.Companion.floorIconsMaster.getOrDefault(floor.lowercase(Locale.getDefault()), PropertyMap())
					} catch (e: Exception) {
						throw RuntimeException(e)
					}
				} else {
					try {
						PartyFinderScreen.Companion.floorIconsNormal.getOrDefault(floor.lowercase(Locale.getDefault()), PropertyMap())
					} catch (e: Exception) {
						throw RuntimeException(e)
					}
				}
			} else if (lowerCase.contains("dungeon:")) {
				dungeon = tooltipText.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }
			} else if (!text.siblings.isEmpty() && text.siblings.first.style.color == TextColor.fromRgb(Formatting.RED.colorValue!!) && !lowerCase.startsWith(" ")) {
				isLocked = true
				lockReason = text
			} else if (lowerCase.contains("note:")) {
				val split = tooltipText.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

				//Note goes onto next line
				if (split.size == 1) {
					val next = tooltips[i + 1].string

					note = if (!next.isBlank() && (!next.contains("Class Level") || !next.contains("Dungeon Level"))) {
						next.trim { it <= ' ' }
					} else {
						""
					}
				} else {
					note = split[1].trim { it <= ' ' }
				}
			}
		}
		if (membersIndex != -1) {
			var i = membersIndex
			var j = 0
			while (i < membersIndex + 5) {
				if (i >= tooltips.size) {
					i++
					j++
					continue
				}

				val text = tooltips[i]
				val memberText = text.string
				if (!memberText.startsWith(" ")) {
					i++
					j++
					continue  // Member thingamajigs start with a space
				}

				val parts = memberText.split(":".toRegex(), limit = 2).toTypedArray()
				val playerNameTrim = parts[0].trim { it <= ' ' }

				if (playerNameTrim == "Empty") {
					i++
					j++
					continue  // Don't care about these idiots lol
				}

				val siblings = text.siblings
				val nameStyle = if (!siblings.isEmpty()) siblings[min(1.0, (siblings.size - 1).toDouble()).toInt()].style else text.style
				val playerName: Text = Text.literal(playerNameTrim).setStyle(nameStyle)
				val className = parts[1].trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
				var classLevel = -1
				val matcher = Pattern.compile("\\((\\d+)\\)").matcher(parts[1])
				if (matcher.find()) classLevel = matcher.group(1).toInt()
				val player = Player(playerName, className, classLevel)

				SkullBlockEntityAccessor.invokeFetchProfileByName(playerNameTrim).thenAccept { gameProfile: Optional<GameProfile?> -> gameProfile.ifPresent { profile: GameProfile? -> player.skinTexture = (client.skinProvider.getSkinTextures(profile).texture()) } }

				if (playerNameTrim == partyHost) {
					partyLeader = player
					j--
				} else if (j > 3) {
					partyLeader = player
				} else partyMembers[j] = player
				i++
				j++
			}
		}

		if (partyLeader == null) {
			for (i in partyMembers.indices.reversed()) {
				if (partyMembers[i] != null) {
					partyLeader = partyMembers[i]
					partyMembers[i] = null
					break
				}
			}
		}
		if (partyLeader == null) {
			partyLeader = Player(Text.literal("Error"), "Error", -1)
		}

		SkullBlockEntityAccessor.invokeFetchProfileByName(partyLeader!!.name.string).thenAccept { gameProfile: Optional<GameProfile?> -> gameProfile.ifPresent { profile: GameProfile? -> partyLeaderSkin = client.skinProvider.getSkinTextures(profile).texture() } }
	}

	override fun selectableChildren(): List<Selectable> {
		returnemptyList()
	}

	override fun children(): List<Element> {
		returnemptyList()
	}

	override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
		val matrices = context.matrices
		matrices.push()
		matrices.translate(x.toFloat(), y.toFloat(), 0f)

		val textRenderer = MinecraftClient.getInstance().textRenderer
		if (hovered && !isLocked) {
			context.drawTexture(PARTY_CARD_TEXTURE_HOVER, 0, 0, 0f, 0f, 336, 64, 336, 64)
			if (this !is YourParty) context.drawText(textRenderer, JOIN_TEXT, 148, 6, -0x1, false)
		} else context.drawTexture(PARTY_CARD_TEXTURE, 0, 0, 0f, 0f, 336, 64, 336, 64)
		val mouseXLocal = mouseX - x
		val mouseYLocal = mouseY - y

		context.drawText(textRenderer, partyLeader!!.toText(), 18, 6, -0x1, true)

		if (PartyFinderScreen.Companion.DEBUG) {
			context.drawText(textRenderer, slotID.toString(), 166, 6, -0x1, true)
			if (hovered) {
				context.drawText(textRenderer, "H", 160, 6, -0x1, true)
			}
		}
		PlayerSkinDrawer.draw(context, partyLeaderSkin, 6, 6, 8, true, false)
		for (i in partyMembers.indices) {
			val partyMember = partyMembers[i] ?: continue
			context.drawTextWithShadow(textRenderer, partyMember.toText(), 17 + 136 * (i % 2), 24 + 14 * (i / 2), -0x1)
			PlayerSkinDrawer.draw(context, partyMember.skinTexture, 6 + 136 * (i % 2), 24 + 14 * (i / 2), 8, true, false)
		}

		if (minClassLevel > 0) {
			context.drawTextWithShadow(textRenderer, Text.of("Class $minClassLevel"), 278, 25, -0x1)
			if (!isLocked && hovered && mouseXLocal >= 276 && mouseXLocal <= 331 && mouseYLocal >= 22 && mouseYLocal <= 35) {
				context.drawTooltip(textRenderer, Text.translatable("skyblocker.partyFinder.partyCard.minClassLevel", minClassLevel), mouseXLocal, mouseYLocal)
			}
		}

		if (minCatacombsLevel > 0) {
			context.drawTextWithShadow(textRenderer, Text.of("Cata $minCatacombsLevel"), 278, 43, -0x1)
			if (!isLocked && hovered && mouseXLocal >= 276 && mouseXLocal <= 331 && mouseYLocal >= 40 && mouseYLocal <= 53) {
				context.drawTooltip(textRenderer, Text.translatable("skyblocker.partyFinder.partyCard.minDungeonLevel", minCatacombsLevel), mouseXLocal, mouseYLocal)
			}
		}
		val stack = ItemStack(Items.PLAYER_HEAD)
		stack.set(DataComponentTypes.PROFILE, SKULL_CACHE.computeIfAbsent("SkyblockerCustomPFSkull$dungeon$floor") { name: String -> ProfileComponent(Optional.of(name), Optional.of(UUID.randomUUID()), floorSkullProperties) })
		context.drawItem(stack, 317, 3)

		val textWidth = textRenderer.getWidth(floor)
		context.drawText(textRenderer, floor, 314 - textWidth, 7, -0x60000000, false)

		context.drawText(textRenderer, note, 5, 52, -0x1, true)

		if (isLocked) {
			matrices.push()
			matrices.translate(0f, 0f, 200f)
			context.fill(0, 0, entryWidth, entryHeight, -0x70000000)
			context.drawText(textRenderer, lockReason, entryWidth / 2 - textRenderer.getWidth(lockReason) / 2, entryHeight / 2 - textRenderer.fontHeight / 2, 0xFFFFFF, true)
			matrices.pop()
		}

		matrices.pop()
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		//System.out.println("To be clicked" + slotID);
		if (slotID == -1) {
			PartyFinderScreen.Companion.LOGGER.error("[Skyblocker] Slot ID is null for " + partyLeader!!.name.string + "'s party")
		}
		if (button == 0 && !screen!!.isWaitingForServer && slotID != -1) {
			screen.clickAndWaitForServer(slotID)
			return true
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	class Player internal constructor(val name: Text, val dungeonClass: String, val classLevel: Int) {
		var skinTexture: Identifier = DefaultSkinHelper.getTexture()

		fun toText(): Text {
			val dClass = if (dungeonClass.isEmpty()) '?' else dungeonClass[0]
			return name.copy().append(Text.literal(" $dClass $classLevel").formatted(Formatting.YELLOW))
		}
	}

	class NoParties : PartyEntry(listOf(), null, -1) {
		override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
			return false
		}

		override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
			val textRenderer = MinecraftClient.getInstance().textRenderer
			context.drawCenteredTextWithShadow(textRenderer, Text.translatable("skyblocker.partyFinder.noParties"), x + entryWidth / 2, y + entryHeight / 2 - textRenderer.fontHeight / 2, -0x1)
		}
	}

	class YourParty(tooltips: List<Text>, screen: PartyFinderScreen?, deListSlotId: Int) : PartyEntry(tooltips, screen, deListSlotId) {
		override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
			var hovered = hovered
			super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta)

			val matrices = context.matrices
			matrices.push()
			matrices.translate(x.toFloat(), y.toFloat(), 0f)

			hovered = hovered and (slotID != -1)

			val textRenderer = MinecraftClient.getInstance().textRenderer
			context.drawText(textRenderer, if (hovered) DE_LIST_TEXT else YOUR_PARTY_TEXT, 148, 6, -0x1, false)

			matrices.pop()
		}

		companion object {
			val DE_LIST_TEXT: Text = Text.translatable("skyblocker.partyFinder.deList")
			val YOUR_PARTY_TEXT: Text = Text.translatable("skyblocker.partyFinder.yourParty")
		}
	}

	companion object {
		private val PARTY_CARD_TEXTURE = Identifier(SkyblockerMod.NAMESPACE, "textures/gui/party_card.png")
		private val PARTY_CARD_TEXTURE_HOVER = Identifier(SkyblockerMod.NAMESPACE, "textures/gui/party_card_hover.png")
		val JOIN_TEXT: Text = Text.translatable("skyblocker.partyFinder.join")
		private val SKULL_CACHE: MutableMap<String, ProfileComponent> = Object2ObjectOpenHashMap()
	}
}
