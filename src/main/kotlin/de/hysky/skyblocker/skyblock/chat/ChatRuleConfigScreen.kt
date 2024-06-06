package de.hysky.skyblocker.skyblock.chat

import it.unimi.dsi.fastutil.ints.IntIntPair
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.sound.SoundEvents
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import java.awt.Color
import java.util.function.Consumer
import kotlin.math.max

class ChatRuleConfigScreen(private val parent: Screen, private val chatRuleIndex: Int) : Screen(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen")) {
	private val soundsLookup = mapOf(
		Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.pling") to SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
		Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.cave") to SoundEvents.AMBIENT_CAVE.value(),
		Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.zombie") to SoundEvents.ENTITY_ZOMBIE_AMBIENT,
		Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.crit") to SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
		Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.arrowHit") to SoundEvents.ENTITY_ARROW_HIT_PLAYER,
		Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.amethyst") to SoundEvents.BLOCK_AMETHYST_BLOCK_HIT,
		Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.anvil") to SoundEvents.BLOCK_ANVIL_LAND
	)

	private var buttonWidth = 75

	private val chatRule: ChatRule? = ChatRulesHandler.chatRuleList[chatRuleIndex]
	private var nameInput: TextFieldWidget? = null
	private var filterInput: TextFieldWidget? = null
	private var partialMatchToggle: ButtonWidget? = null
	private var regexToggle: ButtonWidget? = null
	private var ignoreCaseToggle: ButtonWidget? = null
	private var locationsInput: TextFieldWidget? = null
	private var hideMessageToggle: ButtonWidget? = null
	private var actionBarToggle: ButtonWidget? = null
	private var announcementToggle: ButtonWidget? = null
	private var soundsToggle: ButtonWidget? = null
	private var replaceMessageInput: TextFieldWidget? = null

	//textLocations
	private var nameLabelTextPos: IntIntPair? = null
	private var inputsLabelTextPos: IntIntPair? = null
	private var filterLabelTextPos: IntIntPair? = null
	private var partialMatchTextPos: IntIntPair? = null
	private var regexTextPos: IntIntPair? = null
	private var ignoreCaseTextPos: IntIntPair? = null
	private var locationLabelTextPos: IntIntPair? = null
	private var outputsLabelTextPos: IntIntPair? = null
	private var hideMessageTextPos: IntIntPair? = null
	private var actionBarTextPos: IntIntPair? = null
	private var announcementTextPos: IntIntPair? = null
	private var customSoundLabelTextPos: IntIntPair? = null
	private var replaceMessageLabelTextPos: IntIntPair? = null

	private var currentSoundIndex: Int

	init {
		this.currentSoundIndex = fun(): Int {
				chatRule?.customSound ?: return -1 //if no sound just return -1

				val soundOptions = soundsLookup.values.stream().toList()
				val ruleSoundId = chatRule.customSound!!.id

				for (i in soundOptions.indices) {
					if (soundOptions[i].id.compareTo(ruleSoundId) == 0) {
						return i
					}
				}
				//not found
				return -1
			}.invoke()
	}

	override fun init() {
		super.init()
		if (client == null || chatRule == null) return
		//start centered on the X and 1/3 down on the Y
		calculateMaxButtonWidth()
		var currentPos = IntIntPair.of((this.width - this.maxUsedWidth) / 2, ((this.height - this.maxUsedHeight) * 0.33).toInt())

		nameLabelTextPos = currentPos
		var lineXOffset = client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name")) + SPACER_X
		nameInput = TextFieldWidget(client!!.textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 100, 20, Text.of(""))
		nameInput!!.text = chatRule.name
		nameInput!!.tooltip = Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name.@Tooltip"))
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y)

		inputsLabelTextPos = currentPos
		currentPos = IntIntPair.of(currentPos.leftInt() + 10, currentPos.rightInt() + SPACER_Y)

		filterLabelTextPos = currentPos
		lineXOffset = client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter")) + SPACER_X
		filterInput = TextFieldWidget(client!!.textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 200, 20, Text.of(""))
		filterInput!!.setMaxLength(96)
		filterInput!!.text = chatRule.filter
		filterInput!!.tooltip = Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter.@Tooltip"))
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y)
		lineXOffset = 0

		partialMatchTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		lineXOffset += client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch")) + SPACER_X
		partialMatchToggle = ButtonWidget.builder(enabledButtonText(chatRule.isPartialMatch)) {
			chatRule.isPartialMatch = !chatRule.isPartialMatch
			partialMatchToggle!!.message = enabledButtonText(chatRule.isPartialMatch)
		}
			.position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
			.size(buttonWidth, 20)
			.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch.@Tooltip")))
			.build()
		lineXOffset += buttonWidth + SPACER_X
		regexTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		lineXOffset += client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex")) + SPACER_X
		regexToggle = ButtonWidget.builder(enabledButtonText(chatRule.isRegex)) {
			chatRule.isRegex = !chatRule.isRegex
			regexToggle!!.message = enabledButtonText(chatRule.isRegex)
		}
			.position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
			.size(buttonWidth, 20)
			.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex.@Tooltip")))
			.build()
		lineXOffset += buttonWidth + SPACER_X
		ignoreCaseTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		lineXOffset += client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase")) + SPACER_X
		ignoreCaseToggle = ButtonWidget.builder(enabledButtonText(chatRule.isIgnoreCase)) {
			chatRule.isIgnoreCase = !chatRule.isIgnoreCase
			ignoreCaseToggle!!.message = enabledButtonText(chatRule.isIgnoreCase)
		}
			.position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
			.size(buttonWidth, 20)
			.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase.@Tooltip")))
			.build()
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y)

		locationLabelTextPos = currentPos
		lineXOffset = client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations")) + SPACER_X
		locationsInput = TextFieldWidget(client!!.textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 200, 20, Text.of(""))
		locationsInput!!.setMaxLength(96)
		locationsInput!!.text = chatRule.validLocations
		val locationToolTip = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations.@Tooltip")
		locationToolTip.append("\n")
		ChatRulesHandler.locationsList.forEach(Consumer { location: String? -> locationToolTip.append(" $location,\n") })
		locationsInput!!.tooltip = Tooltip.of(locationToolTip)
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y)

		outputsLabelTextPos = IntIntPair.of(currentPos.leftInt() - 10, currentPos.rightInt())
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y)

		hideMessageTextPos = currentPos
		lineXOffset = client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage")) + SPACER_X
		hideMessageToggle = ButtonWidget.builder(enabledButtonText(chatRule.hideMessage)) {
			chatRule.hideMessage = !chatRule.hideMessage
			hideMessageToggle!!.message = enabledButtonText(chatRule.hideMessage)
		}
			.position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
			.size(buttonWidth, 20)
			.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
			.build()
		lineXOffset += buttonWidth + SPACER_X
		actionBarTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		lineXOffset += client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar")) + SPACER_X
		actionBarToggle = ButtonWidget.builder(enabledButtonText(chatRule.showActionBar)) {
			chatRule.showActionBar = !chatRule.showActionBar
			actionBarToggle!!.message = enabledButtonText(chatRule.showActionBar)
		}
			.position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
			.size(buttonWidth, 20)
			.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar.@Tooltip")))
			.build()
		lineXOffset = 0
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y)

		announcementTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		lineXOffset += client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement")) + SPACER_X
		announcementToggle = ButtonWidget.builder(enabledButtonText(chatRule.showAnnouncement)) {
			chatRule.showAnnouncement = !chatRule.showAnnouncement
			announcementToggle!!.message = enabledButtonText(chatRule.showAnnouncement)
		}
			.position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
			.size(buttonWidth, 20)
			.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.@Tooltip")))
			.build()
		lineXOffset += buttonWidth + SPACER_X
		customSoundLabelTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		lineXOffset += client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds")) + SPACER_X
		soundsToggle = ButtonWidget.builder(soundName) {
			currentSoundIndex += 1
			if (currentSoundIndex == soundsLookup.size) {
				currentSoundIndex = -1
			}
			val newText = soundName
			soundsToggle!!.message = newText
			val sound = soundsLookup[newText]
			chatRule.customSound = sound
			if (client!!.player != null && sound != null) {
				client!!.player!!.playSound(sound, 100f, 0.1f)
			}
		}
			.position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
			.size(buttonWidth, 20)
			.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.@Tooltip")))
			.build()
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y)

		replaceMessageLabelTextPos = currentPos
		lineXOffset = client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace")) + SPACER_X
		replaceMessageInput = TextFieldWidget(client!!.textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 200, 20, Text.of(""))
		replaceMessageInput!!.setMaxLength(96)
		replaceMessageInput!!.tooltip = Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace.@Tooltip"))
		replaceMessageInput!!.text = chatRule.replaceMessage

		val finishButton = ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.finish")) { close() }
			.position(this.width - buttonWidth - SPACER_Y, this.height - SPACER_Y)
			.size(buttonWidth, 20)
			.build()

		addDrawableChild(nameInput)
		addDrawableChild(filterInput)
		addDrawableChild(partialMatchToggle)
		addDrawableChild(regexToggle)
		addDrawableChild(ignoreCaseToggle)
		addDrawableChild(locationsInput)
		addDrawableChild(hideMessageToggle)
		addDrawableChild(actionBarToggle)
		addDrawableChild(announcementToggle)
		addDrawableChild(soundsToggle)
		addDrawableChild(replaceMessageInput)
		addDrawableChild(finishButton)
	}

	/**
	 * if the maxUsedWidth is above the available width decrease the button width to fix this
	 */
	private fun calculateMaxButtonWidth() {
		if (client == null || client!!.currentScreen == null) return
		buttonWidth = 75
		val available = client!!.currentScreen!!.width - maxUsedWidth - SPACER_X * 2
		if (available >= 0) return  //keep the largest size if room

		buttonWidth += available / 3 //remove the needed width from the width of the total 3 buttons
		buttonWidth = max(10.0, buttonWidth.toDouble()).toInt() //do not let the width go below 10
	}

	private val maxUsedWidth: Int
		/**
		 * Works out the width of the maximum line
		 * @return the max used width
		 */
		get() {
			if (client == null) return 0
			//text
			var total = client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch"))
			total += client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex"))
			total += client!!.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase"))
			//space
			total += SPACER_X * 6
			//button width
			total += buttonWidth * 3
			return total
		}

	private val maxUsedHeight: Int
		/**
		 * Works out the height used
		 * @return height used by the gui
		 */
		get() =//there are 8 rows so just times the spacer by 8
			SPACER_Y * 8

	private fun enabledButtonText(enabled: Boolean): Text {
		return if (enabled) {
			Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.true").withColor(Color.GREEN.rgb)
		} else {
			Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.false").withColor(Color.RED.rgb)
		}
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, -0x1)

		//draw labels ands text
		val yOffset = (SPACER_Y - textRenderer.fontHeight) / 2
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.inputs"), inputsLabelTextPos!!.leftInt(), inputsLabelTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name"), nameLabelTextPos!!.leftInt(), nameLabelTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter"), filterLabelTextPos!!.leftInt(), filterLabelTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch"), partialMatchTextPos!!.leftInt(), partialMatchTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex"), regexTextPos!!.leftInt(), regexTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase"), ignoreCaseTextPos!!.leftInt(), ignoreCaseTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations"), locationLabelTextPos!!.leftInt(), locationLabelTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputs"), outputsLabelTextPos!!.leftInt(), outputsLabelTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage"), hideMessageTextPos!!.leftInt(), hideMessageTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar"), actionBarTextPos!!.leftInt(), actionBarTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement"), announcementTextPos!!.leftInt(), announcementTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds"), customSoundLabelTextPos!!.leftInt(), customSoundLabelTextPos!!.rightInt() + yOffset, -0x1)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace"), replaceMessageLabelTextPos!!.leftInt(), replaceMessageLabelTextPos!!.rightInt() + yOffset, -0x1)
	}

	/**
	 * Saves and returns to parent screen
	 */
	override fun close() {
		if (client != null) {
			save()
			client!!.setScreen(parent)
		}
	}

	private fun save() {
		chatRule?.name = nameInput!!.text
		chatRule?.filter = filterInput!!.text
		chatRule?.replaceMessage = replaceMessageInput!!.text
		chatRule?.validLocations = locationsInput!!.text

		ChatRulesHandler.chatRuleList[chatRuleIndex] = chatRule
	}

	private val soundName: MutableText
		get() = if (currentSoundIndex == -1) Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.none") else soundsLookup.keys.stream().toList()[currentSoundIndex]

	companion object {
		private const val SPACER_X = 5
		private const val SPACER_Y = 25
	}
}
