package de.hysky.skyblocker.config

import de.hysky.skyblocker.skyblock.tabhud.widget.Widget
import de.hysky.skyblocker.utils.render.RenderHelper.pointIsInArea
import it.unimi.dsi.fastutil.ints.IntIntMutablePair
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.awt.Color

/**
 * A screen for configuring the positions of HUD widgets.
 *
 *
 * This class takes care of rendering the widgets, dragging them, and resetting their positions.
 * Create one subclass for each collection of HUD widgets that are displayed at the same time.
 * (i.e. one for dwarven mines, one for the end, etc.) See an implementation for an example.
 */
abstract class HudConfigScreen(title: Text, private val parent: Screen?, private val widgets: List<Widget>) : Screen(title) {
	private var draggingWidget: Widget? = null
	private var mouseClickRelativeX = 0.0
	private var mouseClickRelativeY = 0.0

	/**
	 * Creates a new HudConfigScreen with the passed title, parent, and widget
	 * @param title the title of the screen
	 * @param parent the parent screen
	 * @param widget the widget to configure
	 */
	constructor(title: Text, parent: Screen?, widget: Widget) : this(title, parent, listOf(widget))

	/**
	 * Creates a new HudConfigScreen with the passed title, parent, and widgets
	 * @param title the title of the screen
	 * @param parent the parent screen
	 * @param widgets the widgets to configure
	 */
	init {
		resetPos()
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		renderWidget(context, widgets)
		context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.rgb)
	}

	/**
	 * Renders the widgets using the default [Widget.render] method. Override to change the behavior.
	 * @param context the context to render in
	 * @param widgets the widgets to render
	 */
	protected open fun renderWidget(context: DrawContext, widgets: List<Widget>) {
		for (widget in widgets) {
			widget.render(context, SkyblockerConfigManager.config.uiAndVisuals.tabHud.enableHudBackground)
		}
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (button == 0 && draggingWidget != null) {
			draggingWidget!!.x = Math.clamp(mouseX - mouseClickRelativeX, 0.0, (this.width - draggingWidget!!.width).toDouble()).toInt()
			draggingWidget!!.y = Math.clamp(mouseY - mouseClickRelativeY, 0.0, (this.height - draggingWidget!!.height).toDouble()).toInt()
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (button == 0) {
			for (widget in widgets) {
				if (pointIsInArea(mouseX, mouseY, widget.x.toDouble(), widget.y.toDouble(), (widget.x + widget.width).toDouble(), (widget.y + widget.height).toDouble())) {
					draggingWidget = widget
					mouseClickRelativeX = mouseX - widget.x
					mouseClickRelativeY = mouseY - widget.y
					break
				}
			}
		} else if (button == 1) {
			resetPos()
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		draggingWidget = null
		return super.mouseReleased(mouseX, mouseY, button)
	}

	/**
	 * Resets the positions of the widgets to the positions in the config. Override to change the behavior.
	 */
	protected fun resetPos() {
		val configPositions = getConfigPos(SkyblockerConfigManager.config)
		check(configPositions.size == widgets.size) { "The number of positions (" + configPositions.size + ") does not match the number of widgets (" + widgets.size + ")" }
		for (i in widgets.indices) {
			val widget = widgets[i]
			val configPos = configPositions[i]
			widget.x = configPos.leftInt()
			widget.y = configPos.rightInt()
		}
	}

	/**
	 * Returns the positions of the widgets in the config
	 * @param config the config to get the positions from
	 * @return the positions of the widgets
	 */
	protected abstract fun getConfigPos(config: SkyblockerConfig): List<IntIntMutablePair>

	override fun close() {
		val skyblockerConfig = SkyblockerConfigManager.config
		savePos(skyblockerConfig, widgets)
		SkyblockerConfigManager.save()

		client!!.setScreen(parent)
	}

	/**
	 * Saves the passed positions to the config.
	 *
	 *
	 * NOTE: The parent class will call [SkyblockerConfigManager.save] right after this method
	 * @param configManager the config so you don't have to get it
	 * @param widgets the widgets to save
	 */
	protected abstract fun savePos(configManager: SkyblockerConfig, widgets: List<Widget>)
}
