package de.hysky.skyblocker.skyblock.hud.elements;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.skyblock.hud.screens.UIElementConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public abstract class UIElement {
	protected int x, y;
	private final int width, height;
	protected float scale;
	private State state = State.INACTIVE;
	protected double heldFromX, heldFromY;
	private static final Identifier SCALE_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/scaleicon.png");

	/**
	 * @param x      The x position of the element.
	 * @param y      The y position of the element.
	 * @param width  The width of the element. Do not scale this value, it will be scaled automatically.
	 * @param height The height of the element. Do not scale this value, it will be scaled automatically.
	 * @param scale  The scale of the element.
	 */
	protected UIElement(int x, int y, int width, int height, float scale) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.scale = scale;
	}

	/**
	 * Utility constructor for square elements.
	 *
	 * @param x     The x position of the element.
	 * @param y     The y position of the element.
	 * @param size  The size of the element. Do not scale this value, it will be scaled automatically.
	 * @param scale The scale of the element.
	 */
	protected UIElement(int x, int y, int size, float scale) {
		this.x = x;
		this.y = y;
		this.width = size;
		this.height = size;
		this.scale = scale;
	}

	public void setState(State state) {
		this.state = state;
	}

	public State getState() {
		return this.state;
	}

	public void render(DrawContext context, float tickdelta) {
		if (shouldRender()) renderNormalElement(context, tickdelta);
	}


	public abstract void init();

	/**
	 * This method will be called by the config screen when this element is clicked.
	 *
	 * @param mouseX The x position of the mouse.
	 * @param mouseY The y position of the mouse.
	 * @param button The button that was clicked. See {@link GLFW} for the button codes.
	 * @return true if the mouse click was handled by this element, false otherwise.
	 */
	public boolean onMouseClick(double mouseX, double mouseY, int button) {
		heldFromX = mouseX - x;
		heldFromY = mouseY - y;
		if (getScaledWidth() - heldFromX < 8 && getScaledHeight() - heldFromY < 8) state = State.SCALING;
		else state = State.DRAGGING;
		return true;
	}

	/**
	 * This method will be called by the config screen when a mouse button is released while the cursor is over this element.
	 *
	 * @param mouseX The x position of the mouse.
	 * @param mouseY The y position of the mouse.
	 * @param button The button that was released. See {@link GLFW} for the button codes.
	 * @return true if the mouse release was handled by this element, false otherwise.
	 */
	public boolean onMouseRelease(double mouseX, double mouseY, int button) {
		if (state == State.SCALING || state == State.DRAGGING) state = State.SELECTED;
		return true;
	}


	/**
	 * This method will be called by the config screen when this element is dragged,
	 * and will continue to be called until the mouse button is released even if the mouse isn't on the element.
	 *
	 * @param mouseX The x position of the mouse.
	 * @param mouseY The y position of the mouse.
	 * @param button The button that was dragged. See {@link GLFW} for the button codes.
	 * @return true if the mouse drag was handled by this element, false otherwise.
	 */
	public boolean onMouseDrag(double mouseX, double mouseY, int button) {
		if (state == State.SCALING) {
			double distance = Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
			scale = (float) MathHelper.clamp(Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2)) / distance, 0.1, 4);
		} else {
			x = (int) MathHelper.clamp(mouseX - heldFromX, 0, MinecraftClient.getInstance().getWindow().getScaledWidth() - this.getScaledWidth());
			y = (int) MathHelper.clamp(mouseY - heldFromY, 0, MinecraftClient.getInstance().getWindow().getScaledHeight() - this.getScaledHeight());
		}
		return true;
	}

	/**
	 * This method will be called by the config screen when the mouse is moved while hovering on this element.
	 *
	 * @param mouseX The x position of the mouse.
	 * @param mouseY The y position of the mouse.
	 */
	public void onMouseMove(double mouseX, double mouseY) {
	}

	public int getScaledWidth() {
		return (int) (width * scale);
	}

	public int getScaledHeight() {
		return (int) (height * scale);
	}

	/**
	 * Checks if the mouse's coordinates are within the bounds of this element.
	 *
	 * @param mouseX The x position of the mouse.
	 * @param mouseY The y position of the mouse.
	 * @return true if the mouse is over this element, false otherwise.
	 */
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + this.getScaledWidth() && mouseY >= y && mouseY <= y + this.getScaledHeight();
	}

	/**
	 * @implNote Only set the relevant fields in the config to the values of this object and leave saving to the config screen.
	 * @see UIElementConfigScreen#close()
	 */
	public abstract void saveToConfig();

	/**
	 * Reverts the position of the element to the last saved position.
	 */
	public abstract void revertPositionChange();

	/**
	 * Resets the position of the element to the default position as specified in the config.
	 *
	 * @see SkyblockerConfigManager#getDefaults()
	 */
	public abstract void resetPosition();

	/**
	 * For rendering the element in the HUD.
	 *
	 * @see HudRenderEvents
	 */
	public abstract void renderNormalElement(DrawContext context, float tickdelta);

	public abstract boolean shouldRender();

	/**
	 * For rendering the element in the config screen.
	 * <p>This method will be called from the config screen that has this element in its list.</p>
	 * @param context The draw context.
	 * @param tickdelta Progress for lerping.
	 *
	 * @see UIElementConfigScreen#render(DrawContext, int, int, float)
	 */
	public void renderConfigElement(DrawContext context, float tickdelta) {
		renderNormalElement(context, tickdelta);
	}

	/**
	 * Renders a border around the element with a color based on its state.
	 * <p>This method will be called by the config screen.</p>
	 * @param context The draw context.
	 * @see UIElementConfigScreen#render(DrawContext, int, int, float)
	 */
	public void renderBorder(DrawContext context) {
		context.drawBorder(x, y, this.getScaledWidth(), this.getScaledHeight(), state.color);
	}

	/**
	 * Renders the draggable scale icon at the bottom right corner of the element, which allows resizing the element.
	 * <p>This method will be called by the config screen.</p>
	 * @param context The draw context.
	 * @see UIElementConfigScreen#render(DrawContext, int, int, float)
	 */
	public void renderScale(DrawContext context) {
		context.drawTexture(SCALE_TEXTURE, x + getScaledWidth() - 8, y + getScaledHeight() - 8, 0, 0f, 0f, 8, 8, 8, 8);
	}

	public enum State {
		SELECTED(0xFFFFFFFF),
		HOVERED(0xAACCCCCC),
		INACTIVE(0x99999999),
		DRAGGING(0xAA34EB56),
		SCALING(0xAA34A4EB);

		final int color;

		State(int color) {
			this.color = color;
		}
	}
}
