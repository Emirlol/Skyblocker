package de.hysky.skyblocker.skyblock.dungeon.partyfinder

import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyEntry.NoParties
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyEntry.YourParty
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.text.Text
import java.util.*

class PartyEntryListWidget(minecraftClient: MinecraftClient?, width: Int, height: Int, y: Int, itemHeight: Int) : ElementListWidget<PartyEntry?>(minecraftClient, width, height, y, itemHeight) {
	protected var partyEntries: List<PartyEntry>? = null

	var isActive: Boolean = true

	private var search = ""

	override fun getRowWidth(): Int {
		return 336
	}

	fun setEntries(partyEntries: List<PartyEntry>?) {
		this.partyEntries = partyEntries
		updateDisplay()
	}

	fun updateDisplay() {
		val entries: MutableList<PartyEntry> = ArrayList(partyEntries)
		entries.removeIf { partyEntry: PartyEntry -> !partyEntry.note.lowercase(Locale.getDefault()).contains(search) && partyEntry !is YourParty }
		entries.sort(Comparator.comparing { obj: PartyEntry -> obj.isLocked() })
		entries.sort(Comparator.comparing { partyEntry: PartyEntry? -> partyEntry !is YourParty })
		if (entries.isEmpty() && !partyEntries!!.isEmpty()) {
			entries.add(NoParties())
		}
		replaceEntries(entries)
	}

	fun setSearch(s: String) {
		search = s.lowercase(Locale.getDefault())
		updateDisplay()
	}

	override fun getScrollbarX(): Int {
		return this.width / 2 + (rowWidth / 2) + 2
	}


	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!visible) return false
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		//context.drawGuiTexture(BACKGROUND_TEXTURE, x, top-8, getRowWidth()+16+6, bottom-top+16);

		if (children().isEmpty()) {
			val string: Text = Text.translatable("skyblocker.partyFinder.loadingError")
			val textRenderer = MinecraftClient.getInstance().textRenderer
			context.drawTextWrapped(textRenderer, string, rowLeft, y + 10, rowWidth, -0x1)
		} else super.renderWidget(context, mouseX, mouseY, delta)
	}

	override fun drawHeaderAndFooterSeparators(context: DrawContext) {
	}

	override fun drawMenuListBackground(context: DrawContext) {
	}

	companion object {
		var BASE_SKULL_NBT: String = """
              {
              "SkullOwner": {
                "Id": [
                        1215241996,
                        -1849412511,
                        -1161255720,
                        -889217537
                      ],
                "Properties": {
                  "textures": [
                    {
                      "Value": "%TEXTURE%"
                    }
                  ]
                }
              }
            }
            
            """.trimIndent()
	}
}
