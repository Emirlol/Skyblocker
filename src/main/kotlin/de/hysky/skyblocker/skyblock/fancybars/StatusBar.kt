package de.hysky.skyblocker.skyblock.fancybars

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.fancybars.BarPositioner.BarAnchor
import de.hysky.skyblocker.utils.render.RenderHelper.renderNineSliceColored
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.apache.commons.lang3.builder.ToStringBuilder
import java.awt.Color
import java.util.function.Consumer

class StatusBar @JvmOverloads constructor(/* public static final Codec<StatusBar> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("size").forGetter(bar -> bar.size),
                    Codec.INT.fieldOf("x").forGetter(bar -> bar.gridX),
                    Codec.INT.fieldOf("y").forGetter(bar -> bar.gridY),
                    Codec.STRING.listOf().fieldOf("colors").xmap(
                                    strings -> strings.stream().map(s -> Integer.parseInt(s, 16)).map(Color::new).toArray(Color[]::new),
                                    colors -> Arrays.stream(colors).map(color -> Integer.toHexString(color.getRGB())).toList())
                            .forGetter(StatusBar::getColors),
                    Codec.STRING.optionalFieldOf("text_color").xmap(
                                    s -> {
                                        if (s.isPresent()) {
                                            return Optional.of(new Color(Integer.parseInt(s.get(), 16)));
                                        } else return Optional.empty();
                                    },
                                    o -> o.map(object -> Integer.toHexString(((Color) object).getRGB())))
                            .forGetter(bar -> {
                                if (bar.getTextColor() != null) {
                                    return Optional.of(bar.getTextColor());
                                } else return Optional.empty();
                            }),
                    Codec.BOOL.optionalFieldOf("show_text", true).forGetter(StatusBar::showText),
                    Codec.STRING.fieldOf("icon_position").xmap(
                            IconPosition::valueOf,
                            Enum::toString
                    ).forGetter(bar -> bar.iconPosition)
            )

            .apply(instance, ));*/private val icon: Identifier, var colors: Array<Color?>, private val hasOverflow: Boolean, var textColor: Color?, val name: Text = Text.empty()
) : Widget, Drawable, Element, Selectable {
	fun hasOverflow(): Boolean {
		return hasOverflow
	}

	private var onClick: OnClick? = null
	var gridX: Int = 0
	var gridY: Int = 0
	var anchor: BarAnchor? = null

	var size: Int = 1
	private var width = 0

	var fill: Float = 0f
	var overflowFill: Float = 0f
	var inMouse: Boolean = false

	private var value: Any = ""

	private var x = 0
	private var y = 0

	var iconPosition: IconPosition? = IconPosition.LEFT
	private var showText = true

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (width <= 0) return
		// half works lol. only puts transparency on the filler of the bar
		if (inMouse) context.setShaderColor(1f, 1f, 1f, 0.25f)
		when (iconPosition) {
			IconPosition.LEFT -> context.drawGuiTexture(icon, x, y, 9, 9)
			IconPosition.RIGHT -> context.drawGuiTexture(icon, x + width - 9, y, 9, 9)
		}
		val barWith = if (iconPosition == IconPosition.OFF) width else width - 10
		val barX = if (iconPosition == IconPosition.LEFT) x + 10 else x
		context.drawGuiTexture(BAR_BACK, barX, y + 1, barWith, 7)
		renderNineSliceColored(context, BAR_FILL, barX + 1, y + 2, ((barWith - 2) * fill).toInt(), 5, colors[0]!!)


		if (hasOverflow && overflowFill > 0) {
			renderNineSliceColored(context, BAR_FILL, barX + 1, y + 2, ((barWith - 2) * overflowFill).toInt(), 5, colors[1]!!)
		}
		if (inMouse) context.setShaderColor(1f, 1f, 1f, 1f)
		//context.drawText(MinecraftClient.getInstance().textRenderer, gridX + " " + gridY + " s:" + size , x, y-9, Colors.WHITE, true);
	}

	fun updateValues(fill: Float, overflowFill: Float, text: Any) {
		this.value = text
		this.fill = fill
		this.overflowFill = overflowFill
	}

	fun renderText(context: DrawContext) {
		val textRenderer = MinecraftClient.getInstance().textRenderer
		val barWith = if (iconPosition == IconPosition.OFF) width else width - 10
		val barX = if (iconPosition == IconPosition.LEFT) x + 11 else x
		val text = value.toString()
		val x = barX + (barWith - textRenderer.getWidth(text)) / 2
		val y = this.y - 3

		val offsets = intArrayOf(-1, 1)
		for (i in offsets) {
			context.drawText(textRenderer, text, x + i, y, 0, false)
			context.drawText(textRenderer, text, x, y + i, 0, false)
		}
		context.drawText(textRenderer, text, x, y, (if (textColor == null) colors[0] else textColor)!!.rgb, false)
	}

	fun renderCursor(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		val temp_x = x
		val temp_y = y
		val temp_width = width
		val temp_ghost = inMouse

		x = mouseX
		y = mouseY
		width = 100
		inMouse = false

		render(context, mouseX, mouseY, delta)

		x = temp_x
		y = temp_y
		width = temp_width
		inMouse = temp_ghost
	}

	// GUI shenanigans
	override fun setX(x: Int) {
		this.x = x
	}

	override fun setY(y: Int) {
		this.y = y
	}

	override fun getX(): Int {
		return x
	}

	override fun getY(): Int {
		return y
	}

	override fun getWidth(): Int {
		return width
	}

	fun setWidth(width: Int) {
		this.width = width
	}

	override fun getHeight(): Int {
		return 9
	}

	override fun getNavigationFocus(): ScreenRect {
		return super<Widget>.getNavigationFocus()
	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		return mouseX >= x && mouseX <= x + getWidth() && mouseY >= y && mouseY <= y + height
	}

	override fun forEachChild(consumer: Consumer<ClickableWidget>) {
	}

	override fun setFocused(focused: Boolean) {
	}

	override fun isFocused(): Boolean {
		return false
	}

	override fun getType(): Selectable.SelectionType {
		return Selectable.SelectionType.NONE
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (!isMouseOver(mouseX, mouseY)) return false
		if (onClick != null) {
			onClick!!.onClick(this, button, mouseX.toInt(), mouseY.toInt())
		}
		return true
	}

	fun setOnClick(onClick: OnClick?) {
		this.onClick = onClick
	}

	override fun appendNarrations(builder: NarrationMessageBuilder) {
	}

	override fun toString(): String {
		return ToStringBuilder(this)
			.append("name", name)
			.append("gridX", gridX)
			.append("gridY", gridY)
			.append("size", size)
			.append("x", x)
			.append("y", y)
			.append("width", width)
			.append("anchor", anchor)
			.toString()
	}

	fun showText(): Boolean {
		return showText
	}

	fun setShowText(showText: Boolean) {
		this.showText = showText
	}

	enum class IconPosition {
		LEFT,
		RIGHT,
		OFF
	}

	fun interface OnClick {
		fun onClick(statusBar: StatusBar?, button: Int, mouseX: Int, mouseY: Int)
	}

	fun loadFromJson(`object`: JsonObject) {
		// Make colors optional, so it's easy to reset to default
		if (`object`.has("colors")) {
			val colors1 = `object`["colors"].asJsonArray
			check(!(colors1.size() < 2 && hasOverflow)) { "Missing second color of bar that has overflow" }
			val newColors = arrayOfNulls<Color>(colors1.size())
			for (i in 0 until colors1.size()) {
				val jsonElement = colors1[i]
				newColors[i] = Color(jsonElement.asString.toInt(16))
			}
			this.colors = newColors
		}

		if (`object`.has("text_color")) this.textColor = Color(`object`["text_color"].asString.toInt(16))

		val maybeAnchor = `object`["anchor"].asString.trim { it <= ' ' }
		this.anchor = if (maybeAnchor == "null") null else BarAnchor.valueOf(maybeAnchor)
		this.size = `object`["size"].asInt
		this.gridX = `object`["x"].asInt
		this.gridY = `object`["y"].asInt
		// these are optional too, why not
		if (`object`.has("icon_position")) this.iconPosition = IconPosition.valueOf(`object`["icon_position"].asString.trim { it <= ' ' })
		if (`object`.has("show_text")) this.showText = `object`["show_text"].asBoolean
	}

	fun toJson(): JsonObject {
		val `object` = JsonObject()
		val colors1 = JsonArray()
		for (color in colors) {
			colors1.add(Integer.toHexString(color!!.rgb).substring(2))
		}
		`object`.add("colors", colors1)
		if (textColor != null) {
			`object`.addProperty("text_color", Integer.toHexString(textColor!!.rgb).substring(2))
		}
		`object`.addProperty("size", size)
		if (anchor != null) {
			`object`.addProperty("anchor", anchor.toString())
		} else `object`.addProperty("anchor", "null")
		`object`.addProperty("x", gridX)
		`object`.addProperty("y", gridY)
		`object`.addProperty("icon_position", iconPosition.toString())
		`object`.addProperty("show_text", showText)
		return `object`
	}

	companion object {
		private val BAR_FILL = Identifier(SkyblockerMod.NAMESPACE, "bars/bar_fill")
		private val BAR_BACK = Identifier(SkyblockerMod.NAMESPACE, "bars/bar_back")
	}
}
