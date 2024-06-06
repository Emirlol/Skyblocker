package de.hysky.skyblocker.skyblock.chat

import de.hysky.skyblocker.skyblock.chat.ChatRulesConfigListWidget.AbstractChatRuleEntry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.*
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import java.awt.Color
import java.util.function.Function
import java.util.function.Predicate
import kotlin.math.max

class ChatRulesConfigListWidget(client: MinecraftClient?, private val screen: ChatRulesConfigScreen, width: Int, height: Int, y: Int, itemHeight: Int) : ElementListWidget<AbstractChatRuleEntry?>(client, width, height, y, itemHeight) {
	private var hasChanged = false

	init {
		//add labels
		addEntry(ChatRuleLabelsEntry())
		//add entry fall all existing rules
		for (i in ChatRulesHandler.chatRuleList.indices) {
			addEntry(ChatRuleConfigEntry(i))
		}
	}

	override fun getRowWidth(): Int {
		return super.getRowWidth() + 100
	}

	override fun getScrollbarX(): Int {
		return super.getScrollbarX() + 50
	}

	fun addRuleAfterSelected() {
		hasChanged = true
		val newIndex = max(children().indexOf(selectedOrNull).toDouble(), 0.0).toInt()

		ChatRulesHandler.chatRuleList.add(newIndex, ChatRule())
		children().add(newIndex + 1, ChatRuleConfigEntry(newIndex))
	}

	protected override fun removeEntry(entry: AbstractChatRuleEntry): Boolean {
		hasChanged = true
		return super.removeEntry(entry)
	}

	fun saveRules() {
		hasChanged = false
		ChatRulesHandler.saveChatRules()
	}

	fun hasChanges(): Boolean {
		return (hasChanged || children().stream().filter(Predicate { obj: AbstractChatRuleEntry? -> ChatRuleConfigEntry::class.java.isInstance(obj) }).map<ChatRuleConfigEntry>(Function { obj: AbstractChatRuleEntry? -> ChatRuleConfigEntry::class.java.cast(obj) }).anyMatch(Predicate { obj: ChatRuleConfigEntry -> obj.isChange() }))
	}

	abstract class AbstractChatRuleEntry : Entry<AbstractChatRuleEntry?>()

	private inner class ChatRuleLabelsEntry : AbstractChatRuleEntry() {
		override fun selectableChildren(): List<Selectable> {
			returnemptyList()
		}

		override fun children(): List<Element> {
			returnemptyList()
		}

		override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleName"), width / 2 - 125, y + 5, -0x1)
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleEnabled"), width / 2, y + 5, -0x1)
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.modify"), width / 2 + 100, y + 5, -0x1)
		}
	}

	private inner class ChatRuleConfigEntry(//data
		private val chatRuleIndex: Int
	) : AbstractChatRuleEntry() {
		private val chatRule: ChatRule? = ChatRulesHandler.chatRuleList[chatRuleIndex]

		private val children: List<Element>

		//widgets
		private val enabledButton: ButtonWidget
		private val openConfigButton: ButtonWidget
		private val deleteButton: ButtonWidget

		//text location
		private val nameX = width / 2 - 125

		//saved data
		private var oldScrollAmount = 0.0


		init {
			enabledButton = ButtonWidget.builder(enabledButtonText()) { a: ButtonWidget? -> toggleEnabled() }
				.size(50, 20)
				.position(width / 2 - 25, 5)
				.build()

			openConfigButton = ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.editRule")) { a: ButtonWidget? ->
				client.setScreen(ChatRuleConfigScreen(screen, chatRuleIndex))
			}
				.size(50, 20)
				.position(width / 2 + 45, 5)
				.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.editRule.@Tooltip")))
				.build()

			deleteButton = ButtonWidget.builder(Text.translatable("selectServer.delete")) { a: ButtonWidget? ->
				oldScrollAmount = scrollAmount
				client.setScreen(ConfirmScreen({ confirmedAction: Boolean -> this.deleteEntry(confirmedAction) }, Text.translatable("skyblocker.config.chat.chatRules.screen.deleteQuestion"), Text.translatable("skyblocker.config.chat.chatRules.screen.deleteWarning", chatRule.getName()), Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL))
			}
				.size(50, 20)
				.position(width / 2 + 105, 5)
				.build()

			children = java.util.List.of(enabledButton, openConfigButton, deleteButton)
		}

		private fun enabledButtonText(): Text {
			return if (chatRule.getEnabled()) {
				Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.true").withColor(Color.GREEN.rgb)
			} else {
				Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.false").withColor(Color.RED.rgb)
			}
		}

		private fun toggleEnabled() {
			hasChanged = true
			chatRule.setEnabled(!chatRule.getEnabled())
			enabledButton.message = enabledButtonText()
		}

		private fun deleteEntry(confirmedAction: Boolean) {
			if (confirmedAction) {
				//delete this
				ChatRulesHandler.chatRuleList.removeAt(chatRuleIndex)
				removeEntry(this)
			}

			client.setScreen(screen)
			scrollAmount = oldScrollAmount
		}

		override fun selectableChildren(): List<Selectable> {
			return java.util.List.of(object : Selectable {
				override fun getType(): Selectable.SelectionType {
					return Selectable.SelectionType.HOVERED
				}

				override fun appendNarrations(builder: NarrationMessageBuilder) {
					builder.put(NarrationPart.TITLE, chatRule.getName())
				}
			})
		}

		override fun children(): List<Element> {
			return children
		}

		override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) { //todo get strings form en_us.json
			//widgets
			enabledButton.y = y
			enabledButton.render(context, mouseX, mouseY, tickDelta)
			openConfigButton.y = y
			openConfigButton.render(context, mouseX, mouseY, tickDelta)
			deleteButton.y = y
			deleteButton.render(context, mouseX, mouseY, tickDelta)
			//text
			context.drawCenteredTextWithShadow(client.textRenderer, chatRule.getName(), nameX, y + 5, -0x1)
		}

		val isChange: Boolean
			get() = (chatRule.getEnabled() != ChatRulesHandler.chatRuleList[chatRuleIndex].enabled)
	}
}
