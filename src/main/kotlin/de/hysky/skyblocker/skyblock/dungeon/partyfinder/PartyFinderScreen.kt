package de.hysky.skyblocker.skyblock.dungeon.partyfinder

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.authlib.properties.PropertyMap
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyEntry.NoParties
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyEntry.YourParty
import de.hysky.skyblocker.utils.ItemUtils.getLore
import de.hysky.skyblocker.utils.ItemUtils.propertyMapWithTexture
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.toast.SystemToast
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.max

class PartyFinderScreen(var handler: GenericContainerScreenHandler, private val inventory: PlayerInventory, private var name: Text) : Screen(name) {
	private var currentPage: Page? = null
	var partyEntryListWidget: PartyEntryListWidget? = null

	var settingsContainer: FinderSettingsContainer? = null
		private set

	private var refreshSlotId = -1

	private var searchField: TextFieldWidget? = null
	private var refreshButton: ButtonWidget? = null

	private var previousPageButton: ButtonWidget? = null
	private var prevPageSlotId = -1

	private var nextPageButton: ButtonWidget? = null
	private var nextPageSlotId = -1

	var partyFinderButton: ButtonWidget? = null
	var partyButtonSlotId: Int = -1

	private var settingsButton: ButtonWidget? = null
	private var settingsButtonSlotId = -1

	private var createPartyButton: ButtonWidget? = null
	private var createPartyButtonSlotId = -1

	private var dirty = false
	private var dirtiedTime: Long = 0
	var justOpenedSign: Boolean = false

	fun markDirty() {
		if (justOpenedSign) return
		dirtiedTime = System.currentTimeMillis()
		dirty = true
	}

	var isWaitingForServer: Boolean = false
		private set

	override fun init() {
		super.init()
		val topRowButtonsHeight = 20

		// Entry list widget, pretty much every position is based on this guy since it centers automagically
		val widget_height = (this.height * 0.8).toInt()
		val entryListTopY = max(43.0, (height * 0.1).toInt().toDouble()).toInt()
		this.partyEntryListWidget = PartyEntryListWidget(client, width, widget_height, entryListTopY, 68)

		// Search field
		this.searchField = TextFieldWidget(textRenderer, partyEntryListWidget!!.rowLeft + 12, entryListTopY - 12, partyEntryListWidget!!.rowWidth - 12 * 3 - 6, 12, Text.literal("Search..."))
		searchField!!.setPlaceholder(SEARCH_TEXT)
		searchField!!.setChangedListener { s: String -> partyEntryListWidget!!.setSearch(s) }
		// Refresh button
		refreshButton = ButtonWidget.builder(Text.literal("⟳").setStyle(Style.EMPTY.withColor(Formatting.GREEN))) { a: ButtonWidget? ->
			if (refreshSlotId != -1) {
				clickAndWaitForServer(refreshSlotId)
			}
		}
			.position(searchField!!.x + searchField!!.width + 12 * 2, searchField!!.y)
			.size(12, 12).build()
		refreshButton.active = false

		// Prev and next page buttons
		previousPageButton = ButtonWidget.builder(Text.literal("←")) { a: ButtonWidget? ->
			if (prevPageSlotId != -1) {
				clickAndWaitForServer(prevPageSlotId)
			}
		}
			.position(searchField!!.x + searchField!!.width, searchField!!.y)
			.size(12, 12).build()
		previousPageButton.active = false
		nextPageButton = ButtonWidget.builder(Text.literal("→")) { a: ButtonWidget? ->
			if (nextPageSlotId != -1) {
				clickAndWaitForServer(nextPageSlotId)
			}
		}
			.position(searchField!!.x + searchField!!.width + 12, searchField!!.y)
			.size(12, 12).build()
		nextPageButton.active = false

		// Settings container
		if (this.settingsContainer == null) this.settingsContainer = FinderSettingsContainer(partyEntryListWidget!!.rowLeft, entryListTopY - 12, widget_height + 12)
		else settingsContainer!!.setDimensionsAndPosition(partyEntryListWidget!!.rowWidth - 2, widget_height + 12, partyEntryListWidget!!.rowLeft, entryListTopY - 12)


		// Buttons at the top
		val searchButtonMargin = 2
		val searchButtonWidth = (partyEntryListWidget!!.rowWidth + 6) / 3 - 2 * searchButtonMargin


		partyFinderButton = ButtonWidget.builder(Text.translatable("skyblocker.partyFinder.tabs.partyFinder")) { a: ButtonWidget? ->
			if (partyButtonSlotId != -1) {
				setCurrentPage(Page.FINDER)
				clickAndWaitForServer(partyButtonSlotId)
			}
		}
			.position(partyEntryListWidget!!.rowLeft, entryListTopY - 39)
			.size(searchButtonWidth + searchButtonMargin, topRowButtonsHeight).build()

		settingsButton = ButtonWidget.builder(Text.translatable("skyblocker.partyFinder.tabs.searchSettings")) { a: ButtonWidget? ->
			if (settingsButtonSlotId != -1) {
				setCurrentPage(Page.SETTINGS)
				clickAndWaitForServer(settingsButtonSlotId)
			}
		}
			.position(partyEntryListWidget!!.rowLeft + searchButtonWidth + 3 * searchButtonMargin, entryListTopY - 39)
			.size(searchButtonWidth, topRowButtonsHeight).build()

		createPartyButton = ButtonWidget.builder(Text.translatable("skyblocker.partyFinder.tabs.createParty")) { a: ButtonWidget? ->
			if (createPartyButtonSlotId != -1) {
				clickAndWaitForServer(createPartyButtonSlotId)
			}
		}
			.position(partyEntryListWidget!!.rowLeft + searchButtonWidth * 2 + 5 * searchButtonMargin, entryListTopY - 39)
			.size(searchButtonWidth, topRowButtonsHeight).build()
		createPartyButton.active = false


		addDrawableChild(partyEntryListWidget)
		addDrawableChild(searchField)
		addDrawableChild(refreshButton)
		addDrawableChild(previousPageButton)
		addDrawableChild(nextPageButton)
		addDrawableChild(partyFinderButton)
		addDrawableChild(settingsButton)
		addDrawableChild(createPartyButton)
		addDrawableChild(settingsContainer)
		addDrawableChild(ButtonWidget.builder(Text.of("DEBUG")) { a: ButtonWidget? -> DEBUG = !DEBUG }.dimensions(width - 40, 0, 40, 20).build())

		dirtiedTime = System.currentTimeMillis()


		// Used when resizing
		setCurrentPage(currentPage)

		if (currentPage != Page.SIGN) update()
	}

	override fun shouldPause(): Boolean {
		return false
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		if (searchField!!.visible) {
			context.drawGuiTexture(SEARCH_ICON_TEXTURE, partyEntryListWidget!!.rowLeft + 1, searchField!!.y + 1, 10, 10)
		}
		if (DEBUG) {
			context.drawText(textRenderer, "Truly a party finder", 20, 20, -0x1, true)
			context.drawText(textRenderer, currentPage.toString(), 0, 0, -0x1, true)
			context.drawText(textRenderer, refreshSlotId.toString(), width - 25, 30, -0x1, true)
			context.drawText(textRenderer, prevPageSlotId.toString(), width - 25, 40, -0x1, true)
			context.drawText(textRenderer, nextPageSlotId.toString(), width - 25, 50, -0x1, true)
			for (i in handler.slots.indices) {
				context.drawItem(handler.slots[i].stack, (i % 9) * 16, (i / 9) * 16)
			}
		}
		if (isWaitingForServer) {
			val s = "Waiting for server..."
			context.drawText(textRenderer, s, this.width - textRenderer.getWidth(s) - 5, this.height - textRenderer.fontHeight - 2, -0x1, true)
		}
		if (!settingsContainer!!.canInteract(null)) {
			context.fill(0, 0, width, height, 50, 0x40000000)
		}
	}

	override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.renderBackground(context, mouseX, mouseY, delta)
		val i = partyEntryListWidget!!.rowWidth + 16 + 6
		context.drawGuiTexture(BACKGROUND_TEXTURE, partyEntryListWidget!!.rowLeft - 8, partyEntryListWidget!!.y - 12 - 8, i, partyEntryListWidget!!.bottom - partyEntryListWidget!!.y + 16 + 12)
	}

	override fun close() {
		checkNotNull(this.client)
		checkNotNull(this.client!!.player)
		if (currentPage != Page.SIGN) this.client!!.player!!.closeHandledScreen()
		else {
			val networkHandler = this.client!!.networkHandler
			if (networkHandler != null && sign != null) {
				val originalText = Arrays.stream(sign!!.getText(isSignFront).getMessages(true)).map { obj: Text -> obj.string }.toList()
				networkHandler.sendPacket(UpdateSignC2SPacket(sign!!.pos, isSignFront, originalText.first, originalText[1], originalText[2], originalText[3]))
			}
		}
		super.close()
	}

	fun setCurrentPage(page: Page?) {
		this.currentPage = page
		if (page == Page.FINDER) {
			partyEntryListWidget!!.visible = true

			partyFinderButton!!.active = false
			partyFinderButton!!.message = partyFinderButton!!.message.copy().setStyle(Style.EMPTY.withUnderline(true))
			settingsButton!!.active = true
			settingsButton!!.message = settingsButton!!.message.copy().setStyle(Style.EMPTY.withUnderline(false))
			createPartyButton!!.active = true

			searchField!!.active = true
			searchField!!.visible = true
			settingsContainer.setVisible(false)
			refreshButton!!.visible = true
			previousPageButton!!.visible = true
			nextPageButton!!.visible = true
		} else if (page == Page.SETTINGS || page == Page.SIGN) {
			partyEntryListWidget!!.visible = false

			partyFinderButton!!.active = page != Page.SIGN
			partyFinderButton!!.message = partyFinderButton!!.message.copy().setStyle(Style.EMPTY.withUnderline(false))
			settingsButton!!.active = false
			settingsButton!!.message = settingsButton!!.message.copy().setStyle(Style.EMPTY.withUnderline(true))
			createPartyButton!!.active = false

			searchField!!.active = false
			searchField!!.visible = false
			settingsContainer.setVisible(true)
			refreshButton!!.visible = false
			previousPageButton!!.visible = false
			nextPageButton!!.visible = false
		}
	}

	// Called when the handler object/title gets changed
	fun updateHandler(handler: GenericContainerScreenHandler, name: Text) {
		this.handler = handler
		this.name = name
		markDirty()
	}

	var isSignFront: Boolean = true
		private set
	var sign: SignBlockEntity? = null
		private set

	fun updateSign(sign: SignBlockEntity, front: Boolean) {
		setCurrentPage(Page.SIGN)
		isSignFront = front
		this.sign = sign
		justOpenedSign = true
		isWaitingForServer = false
		if (!settingsContainer!!.handleSign(sign, front)) abort()
	}

	fun update() {
		dirty = false
		isWaitingForServer = false
		val titleText = name.string
		if (titleText.contains("Party Finder")) {
			updatePartyFinderPage()
		} else {
			if (currentPage != Page.SETTINGS) setCurrentPage(Page.SETTINGS)
			if (!settingsContainer!!.handle(this, titleText)) {
				abort()
			}
		}
	}

	private fun updatePartyFinderPage() {
		previousPageButton!!.active = false
		nextPageButton!!.active = false
		val parties: MutableList<PartyEntry> = ArrayList()
		if (currentPage != Page.FINDER) setCurrentPage(Page.FINDER)
		if (handler.slots.stream().anyMatch { slot: Slot -> slot.hasStack() && slot.stack.isOf(Items.BEDROCK) }) {
			parties.add(NoParties())
		} else {
			for (slot in handler.slots) {
				if (slot.id > (handler.rows - 1) * 9 - 1 || !slot.hasStack()) continue
				if (slot.stack.isOf(Items.PLAYER_HEAD)) {
					checkNotNull(this.client)
					parties.add(PartyEntry(getLore(slot.stack), this, slot.id))
				} else if (slot.stack.isOf(Items.ARROW) && slot.stack.name.string.lowercase(Locale.getDefault()).contains("previous")) {
					prevPageSlotId = slot.id
					previousPageButton!!.active = true
				} else if (slot.stack.isOf(Items.ARROW) && slot.stack.name.string.lowercase(Locale.getDefault()).contains("next")) {
					nextPageSlotId = slot.id
					nextPageButton!!.active = true
				}
			}
		}
		var deListSlotId = -1
		var tooltips: MutableList<Text>? = null
		for (i in (handler.rows - 1) * 9 until (handler.rows * 9)) {
			val slot = handler.slots[i]
			if (!slot.hasStack()) continue
			if (slot.stack.isOf(Items.EMERALD_BLOCK)) {
				refreshSlotId = slot.id
				refreshButton!!.active = true
			} else if (slot.stack.isOf(Items.REDSTONE_BLOCK)) {
				createPartyButtonSlotId = slot.id
				createPartyButton!!.active = true
			} else if (slot.stack.isOf(Items.NETHER_STAR)) {
				settingsButtonSlotId = slot.id
				if (DEBUG) settingsButton!!.message = settingsButton!!.message.copy().append(Text.of(" $settingsButtonSlotId"))
			} else if (slot.stack.isOf(Items.BOOKSHELF)) {
				deListSlotId = slot.id
			} else if (slot.stack.isOf(Items.PLAYER_HEAD)) {
				checkNotNull(this.client)
				tooltips = ArrayList(getLore(slot.stack))
			}
		}
		if (tooltips != null) {
			//LOGGER.info("Your Party tooltips");
			//tooltips.forEach(text -> LOGGER.info(text.toString()));
			if (deListSlotId != -1) {
				// Such a wacky thing lol
				tooltips[0] = Text.literal(MinecraftClient.getInstance().session.username + "'s party")
			}
			parties.add(YourParty(tooltips, this, deListSlotId))
		}
		partyEntryListWidget!!.setEntries(parties)

		//List<ItemStack> temp = handler.slots.stream().map(Slot::getStack).toList();//for (int i = 0; i < temp.size(); i++) System.out.println(i + " " + temp.get(i).toString() + " " + temp.get(i).getName().getString());
	}

	var isAborted: Boolean = false
		private set

	fun abort() {
		checkNotNull(this.client)
		if (currentPage == Page.SIGN) {
			checkNotNull(this.client!!.player)
			this.client!!.player!!.openEditSignScreen(sign, isSignFront)
		} else this.client!!.setScreen(GenericContainerScreen(handler, inventory, title))
		this.client!!.toastManager.add(SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("skyblocker.partyFinder.error.name"), Text.translatable("skyblocker.partyFinder.error.message")))
		isAborted = true
	}

	override fun removed() {
		checkNotNull(this.client)
		if (this.client!!.player == null || this.isAborted || (currentPage == Page.SIGN)) {
			return
		}
		(handler as ScreenHandler).onClosed(this.client!!.player)
	}

	override fun tick() {
		super.tick()
		// Slight delay to make sure all slots are received, because they are most of the time sent one at a time
		if (dirty && System.currentTimeMillis() - dirtiedTime > 60) update()
		assert(this.client != null && this.client!!.player != null)
		if (!this.client!!.player!!.isAlive || this.client!!.player!!.isRemoved && currentPage != Page.SIGN) {
			this.client!!.player!!.closeHandledScreen()
		}
	}

	fun clickAndWaitForServer(slotID: Int) {
		//System.out.println("hey");
		checkNotNull(client)
		checkNotNull(client!!.interactionManager)
		client!!.interactionManager!!.clickSlot(handler.syncId, slotID, 0, SlotActionType.PICKUP, client!!.player)
		isWaitingForServer = true
	}

	var client: MinecraftClient
		get() {
			checkNotNull(this.client)
			return this.client!!
		}
		set(client) {
			super.client = client
		}

	enum class Page {
		FINDER,
		SETTINGS,
		SIGN
	}

	companion object {
		val LOGGER: Logger = LoggerFactory.getLogger(PartyFinderScreen::class.java)
		protected val BACKGROUND_TEXTURE: Identifier = Identifier("social_interactions/background")
		protected val SEARCH_ICON_TEXTURE: Identifier = Identifier("icon/search")
		protected val SEARCH_TEXT: Text = Text.translatable("gui.socialInteractions.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY)
		@JvmField
        var isInKuudraPartyFinder: Boolean = false

		var DEBUG: Boolean = false
		@JvmField
        val possibleInventoryNames: List<String> = listOf(
			"party finder",
			"search settings",
			"select floor",
			"select type",
			"class level range",
			"dungeon level range",
			"sort"
		)

		var floorIconsNormal: MutableMap<String, PropertyMap>? = null
		var floorIconsMaster: MutableMap<String, PropertyMap>? = null

		fun initClass() {
			ClientLifecycleEvents.CLIENT_STARTED.register(ClientStarted { client: MinecraftClient ->
				//Checking when this is loaded probably isn't necessary as the maps are always null checked
				CompletableFuture.runAsync {
					floorIconsNormal = HashMap()
					floorIconsMaster = HashMap()
					try {
						client.resourceManager.openAsReader(Identifier(SkyblockerMod.NAMESPACE, "dungeons/catacombs/floorskulls.json")).use { skullTextureReader ->
							val json = SkyblockerMod.GSON.fromJson(skullTextureReader, JsonObject::class.java)
							json.getAsJsonObject("normal").asMap().forEach { (s: String, tex: JsonElement) -> floorIconsNormal[s] = propertyMapWithTexture(tex.asString) }
							json.getAsJsonObject("master").asMap().forEach { (s: String, tex: JsonElement) -> floorIconsMaster[s] = propertyMapWithTexture(tex.asString) }
							LOGGER.debug("[Skyblocker] Dungeons floor skull textures json loaded")
						}
					} catch (e: Exception) {
						LOGGER.error("[Skyblocker] Failed to load dungeons floor skull textures json", e)
					}
				}
			})
		}
	}
}
