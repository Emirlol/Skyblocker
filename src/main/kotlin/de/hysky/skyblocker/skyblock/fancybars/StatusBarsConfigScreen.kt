package de.hysky.skyblocker.skyblock.fancybars

import de.hysky.skyblocker.skyblock.fancybars.BarPositioner.BarAnchor
import de.hysky.skyblocker.skyblock.fancybars.BarPositioner.BarLocation
import it.unimi.dsi.fastutil.Pair
import it.unimi.dsi.fastutil.objects.ObjectBooleanMutablePair
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ScreenPos
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.navigation.NavigationAxis
import net.minecraft.client.gui.navigation.NavigationDirection
import net.minecraft.client.gui.screen.PopupScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.*
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.util.function.Consumer
import kotlin.math.min

class StatusBarsConfigScreen : Screen(Text.of("Status Bars Config")) {
	private val rectToBarLocation: MutableMap<ScreenRect, BarLocation> = HashMap()
	private var cursorBar: StatusBar? = null

	private var currentInsertLocation = BarLocation(null, 0, 0)

	private val resizeHover: Pair<BarLocation?, Boolean> = ObjectBooleanMutablePair<BarLocation?>(BarLocation.Companion.NULL, false)

	private val resizedBars: Pair<BarLocation?, BarLocation?> = ObjectObjectMutablePair.of<BarLocation?, BarLocation?>(BarLocation.Companion.NULL, BarLocation.Companion.NULL)
	private var resizing = false

	private var editBarWidget: EditBarWidget? = null

	init {
		FancyStatusBars.Companion.updatePositions()
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		/*for (ScreenRect screenRect : meaningFullName.keySet()) {
            context.fillGradient(screenRect.position().x(), screenRect.position().y(), screenRect.position().x() + screenRect.width(), screenRect.position().y() + screenRect.height(), 0xFFFF0000, 0xFF0000FF);
        }*/
		super.render(context, mouseX, mouseY, delta)
		context.drawGuiTexture(HOTBAR_TEXTURE, width / 2 - HOTBAR_WIDTH / 2, height - 22, HOTBAR_WIDTH, 22)
		editBarWidget!!.render(context, mouseX, mouseY, delta)

		val mouseRect = ScreenRect(ScreenPos(mouseX - 1, mouseY - 1), 3, 3)
		checkNotNull(client)
		val window = client!!.window

		if (cursorBar != null) {
			cursorBar!!.renderCursor(context, mouseX, mouseY, delta)
			var inserted = false
			rectLoop@ for (screenRect in rectToBarLocation.keys) {
				for (direction in DIRECTION_CHECK_ORDER) {
					val overlaps = screenRect.getBorder(direction).add(direction).overlaps(mouseRect)

					if (overlaps) {
						val barSnap = rectToBarLocation[screenRect]
						if (barSnap!!.barAnchor == null) break
						if (direction.axis == NavigationAxis.VERTICAL) {
							val neighborInsertY = getNeighborInsertY(barSnap, !direction.isPositive)
							if (!currentInsertLocation.equals(barSnap.barAnchor, barSnap.x, neighborInsertY)) {
								if (cursorBar!!.anchor != null) FancyStatusBars.Companion.barPositioner.removeBar(cursorBar!!.anchor!!, cursorBar!!.gridY, cursorBar)
								FancyStatusBars.Companion.barPositioner.addRow(barSnap.barAnchor, neighborInsertY)
								FancyStatusBars.Companion.barPositioner.addBar(barSnap.barAnchor, neighborInsertY, cursorBar!!)
								currentInsertLocation = BarLocation.Companion.of(cursorBar!!)
								inserted = true
							}
						} else {
							val neighborInsertX = getNeighborInsertX(barSnap, direction.isPositive)
							if (!currentInsertLocation.equals(barSnap.barAnchor, neighborInsertX, barSnap.y)) {
								if (cursorBar!!.anchor != null) FancyStatusBars.Companion.barPositioner.removeBar(cursorBar!!.anchor!!, cursorBar!!.gridY, cursorBar)
								FancyStatusBars.Companion.barPositioner.addBar(barSnap.barAnchor, barSnap.y, neighborInsertX, cursorBar!!)
								currentInsertLocation = BarLocation.Companion.of(cursorBar!!)
								inserted = true
							}
						}
						break@rectLoop
					}
				}
			}
			if (inserted) {
				FancyStatusBars.Companion.updatePositions()
				return
			}
			// check for hovering empty anchors
			for (barAnchor in BarAnchor.Companion.allAnchors()) {
				if (FancyStatusBars.Companion.barPositioner.getRowCount(barAnchor) != 0) continue
				val anchorHitbox = barAnchor.getAnchorHitbox(barAnchor.getAnchorPosition(width, height))
				context.fill(anchorHitbox!!.left, anchorHitbox!!.top, anchorHitbox!!.right, anchorHitbox!!.bottom, -0x66000001)
				if (anchorHitbox!!.overlaps(mouseRect) && currentInsertLocation.barAnchor != barAnchor) {
					if (cursorBar!!.anchor != null) FancyStatusBars.Companion.barPositioner.removeBar(cursorBar!!.anchor!!, cursorBar!!.gridY, cursorBar)
					FancyStatusBars.Companion.barPositioner.addRow(barAnchor)
					FancyStatusBars.Companion.barPositioner.addBar(barAnchor, 0, cursorBar!!)
					currentInsertLocation = BarLocation.Companion.of(cursorBar!!)
					FancyStatusBars.Companion.updatePositions()
				}
			}
		} else {
			if (resizing) { // actively resizing one or 2 bars
				val middleX: Int

				val left = resizedBars.left()
				val right = resizedBars.right()
				val hasRight = right!!.barAnchor != null
				val hasLeft = left!!.barAnchor != null
				val barAnchor: BarAnchor?
				if (!hasRight) {
					barAnchor = left.barAnchor
					val bar: StatusBar = FancyStatusBars.Companion.barPositioner.getBar(barAnchor!!, left.y, left.x)
					middleX = bar.x + bar.width
				} else {
					barAnchor = right.barAnchor
					middleX = FancyStatusBars.Companion.barPositioner.getBar(barAnchor!!, right.y, right.x)!!.getX()
				}

				var doResize = true
				var rightBar: StatusBar? = null
				var leftBar: StatusBar? = null

				val sizeRule = barAnchor.sizeRule
				val widthPerSize = if (sizeRule!!.isTargetSize) sizeRule.totalWidth.toFloat() / sizeRule.targetSize
				else sizeRule.widthPerSize.toFloat()

				// resize towards the left
				if (mouseX < middleX) {
					if (middleX - mouseX > widthPerSize / .75f) {
						if (hasRight) {
							rightBar = FancyStatusBars.Companion.barPositioner.getBar(barAnchor, right.y, right.x)
							if (rightBar!!.size + 1 > sizeRule.maxSize) doResize = false
						}
						if (hasLeft) {
							leftBar = FancyStatusBars.Companion.barPositioner.getBar(barAnchor, left.y, left.x)
							if (leftBar!!.size - 1 < sizeRule.minSize) doResize = false
						}

						if (doResize) {
							if (hasRight) rightBar!!.size++
							if (hasLeft) leftBar!!.size--
							FancyStatusBars.Companion.updatePositions()
						}
					}
				} else { // towards the right
					if (mouseX - middleX > widthPerSize / .75f) {
						if (hasRight) {
							rightBar = FancyStatusBars.Companion.barPositioner.getBar(barAnchor, right.y, right.x)
							if (rightBar!!.size - 1 < sizeRule.minSize) doResize = false
						}
						if (hasLeft) {
							leftBar = FancyStatusBars.Companion.barPositioner.getBar(barAnchor, left.y, left.x)
							if (leftBar!!.size + 1 > sizeRule.maxSize) doResize = false
						}

						if (doResize) {
							if (hasRight) rightBar!!.size--
							if (hasLeft) leftBar!!.size++
							FancyStatusBars.Companion.updatePositions()
						}
					}
				}
			} else { // hovering bars
				rectLoop@ for (screenRect in rectToBarLocation.keys) {
					for (direction in arrayOf<NavigationDirection>(NavigationDirection.LEFT, NavigationDirection.RIGHT)) {
						val overlaps = screenRect.getBorder(direction).add(direction).overlaps(mouseRect)

						if (overlaps && !editBarWidget!!.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
							val barLocation = rectToBarLocation[screenRect]
							if (barLocation!!.barAnchor == null) break
							val right = direction == NavigationDirection.RIGHT
							// can't resize on the edge of a target size row!
							if (barLocation.barAnchor.getSizeRule().isTargetSize && !FancyStatusBars.Companion.barPositioner.hasNeighbor(barLocation.barAnchor, barLocation.y, barLocation.x, right)) {
								break
							}
							if (!barLocation.barAnchor.getSizeRule().isTargetSize && barLocation.x == 0 && barLocation.barAnchor.isRight() != right) break
							resizeHover.first(barLocation)
							resizeHover.right(right)
							GLFW.glfwSetCursor(window.handle, RESIZE_CURSOR)
							break@rectLoop
						} else {
							resizeHover.first(BarLocation.Companion.NULL)
							GLFW.glfwSetCursor(window.handle, 0)
						}
					}
				}
			}
		}
	}

	override fun init() {
		super.init()
		editBarWidget = EditBarWidget(0, 0, this)
		editBarWidget!!.visible = false
		addSelectableChild(editBarWidget) // rendering separately to have it above hotbar
		val values: Collection<StatusBar> = FancyStatusBars.Companion.statusBars.values
		values.forEach(Consumer { statusBar: StatusBar -> this.setup(statusBar) })
		checkNullAnchor(values)
		updateScreenRects()
		this.addDrawableChild(
			ButtonWidget.builder(
				Text.literal("?")
			) { button: ButtonWidget? ->
				checkNotNull(client)
				client!!.setScreen(PopupScreen.Builder(this, Text.translatable("skyblocker.bars.config.explanationTitle"))
					.button(Text.translatable("gui.ok")) { obj: PopupScreen -> obj.close() }
					.message(Text.translatable("skyblocker.bars.config.explanation"))
					.build())
			}
				.dimensions(width - 20, (height - 15) / 2, 15, 15)
				.build())
	}

	private fun setup(statusBar: StatusBar) {
		this.addDrawableChild(statusBar)
		statusBar.setOnClick { statusBar: StatusBar?, button: Int, mouseX: Int, mouseY: Int -> this.onBarClick(statusBar, button, mouseX, mouseY) }
	}

	override fun removed() {
		super.removed()
		FancyStatusBars.Companion.statusBars.values.forEach(Consumer<StatusBar> { statusBar: StatusBar -> statusBar.setOnClick(null) })
		if (cursorBar != null) cursorBar!!.inMouse = false
		FancyStatusBars.Companion.updatePositions()
		checkNotNull(client)
		GLFW.glfwSetCursor(client!!.window.handle, 0)
		FancyStatusBars.Companion.saveBarConfig()
	}

	override fun shouldPause(): Boolean {
		return false
	}

	private fun onBarClick(statusBar: StatusBar?, button: Int, mouseX: Int, mouseY: Int) {
		if (button == 0) {
			cursorBar = statusBar
			cursorBar!!.inMouse = true
			if (statusBar!!.anchor != null) FancyStatusBars.Companion.barPositioner.removeBar(statusBar.anchor!!, statusBar.gridY, statusBar)
			FancyStatusBars.Companion.updatePositions()
			cursorBar!!.x = width + 5 // send it to limbo lol
			updateScreenRects()
		} else if (button == 1) {
			val x = min((mouseX - 1).toDouble(), (width - editBarWidget!!.width).toDouble()).toInt()
			val y = min((mouseY - 1).toDouble(), (height - editBarWidget!!.height).toDouble()).toInt()
			editBarWidget!!.visible = true
			editBarWidget!!.setStatusBar(statusBar)
			editBarWidget!!.x = x
			editBarWidget!!.y = y
		}
	}

	private fun updateScreenRects() {
		rectToBarLocation.clear()
		FancyStatusBars.Companion.statusBars.values.forEach(Consumer<StatusBar> { statusBar1: StatusBar ->
			if (statusBar1.anchor == null) return@forEach
			rectToBarLocation[ScreenRect(ScreenPos(statusBar1.x, statusBar1.y), statusBar1.width, statusBar1.height)] = BarLocation.Companion.of(statusBar1)
		})
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (cursorBar != null) {
			cursorBar!!.inMouse = false
			cursorBar = null
			FancyStatusBars.Companion.updatePositions()
			checkNullAnchor(FancyStatusBars.Companion.statusBars.values)
			updateScreenRects()
			return true
		} else if (resizing) {
			resizing = false
			resizedBars.left(BarLocation.Companion.NULL)
			resizedBars.right(BarLocation.Companion.NULL)
			updateScreenRects()
			return true
		}
		return super.mouseReleased(mouseX, mouseY, button)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		val first = resizeHover.first()
		// want the right click thing to have priority
		if (!editBarWidget!!.isMouseOver(mouseX, mouseY) && button == 0 && first != BarLocation.Companion.NULL) {
			val barAnchor = checkNotNull(first!!.barAnchor)
			if (resizeHover.right()) {
				resizedBars.left(first)

				if (FancyStatusBars.Companion.barPositioner.hasNeighbor(barAnchor, first.y, first.x, true)) {
					resizedBars.right(BarLocation(barAnchor, first.x + (if (barAnchor.isRight) 1 else -1), first.y))
				} else resizedBars.right(BarLocation.Companion.NULL)
			} else {
				resizedBars.right(first)

				if (FancyStatusBars.Companion.barPositioner.hasNeighbor(barAnchor, first.y, first.x, false)) {
					resizedBars.left(BarLocation(barAnchor, first.x + (if (barAnchor.isRight) -1 else 1), first.y))
				} else resizedBars.left(BarLocation.Companion.NULL)
			}
			resizing = true
			return true
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	companion object {
		private val HOTBAR_TEXTURE = Identifier("hud/hotbar")

		val RESIZE_CURSOR: Long = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR)

		private const val HOTBAR_WIDTH = 182

		// prioritize left and right cuz they are much smaller space than up and down
		private val DIRECTION_CHECK_ORDER = arrayOf(NavigationDirection.LEFT, NavigationDirection.RIGHT, NavigationDirection.UP, NavigationDirection.DOWN)

		private fun getNeighborInsertX(barLocation: BarLocation?, right: Boolean): Int {
			val barAnchor = barLocation!!.barAnchor
			val gridX = barLocation.x
			if (barAnchor == null) return 0
			return if (right) {
				if (barAnchor.isRight) gridX + 1 else gridX
			} else {
				if (barAnchor.isRight) gridX else gridX + 1
			}
		}

		private fun getNeighborInsertY(barLocation: BarLocation?, up: Boolean): Int {
			val barAnchor = barLocation!!.barAnchor
			val gridY = barLocation.y
			if (barAnchor == null) return 0
			return if (up) {
				if (barAnchor.isUp) gridY + 1 else gridY
			} else {
				if (barAnchor.isUp) gridY else gridY + 1
			}
		}

		private fun checkNullAnchor(bars: Iterable<StatusBar>) {
			var offset = 0
			for (statusBar in bars) {
				if (statusBar.anchor == null) {
					statusBar.x = 5
					statusBar.y = 50 + offset
					statusBar.width = 30
					offset += statusBar.height
				}
			}
		}
	}
}
