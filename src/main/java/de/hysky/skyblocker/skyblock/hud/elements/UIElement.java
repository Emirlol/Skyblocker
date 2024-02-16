package de.hysky.skyblocker.skyblock.hud.elements;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.hud.screens.UIElementConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public abstract class UIElement {
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected float scale;
	private State state = State.INACTIVE;
	private boolean isScaling = false; //Todo: Find a better name for this. 'scaling' can be mistaken for the scale field while 'isScaling' sounds like a getter method for a boolean.
	protected double heldFromX;
	protected double heldFromY;
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

	public boolean onMouseClick(double mouseX, double mouseY, int button) {
		heldFromX = mouseX - x;
		heldFromY = mouseY - y;
		if (getScaledWidth() - heldFromX < 8 && getScaledHeight() - heldFromY < 8) isScaling = true;
		return true;
	}

	public boolean onMouseRelease(double mouseX, double mouseY, int button) {
		isScaling = false;
		return true;
	}

	public boolean onMouseDrag(double mouseX, double mouseY, int button) {
		if (isScaling) {

			double distance = Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
			scale = (float) MathHelper.clamp(Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2)) / distance, 0.1, 4);
		} else {
			x = (int) MathHelper.clamp(mouseX - heldFromX, 0, MinecraftClient.getInstance().getWindow().getScaledWidth() - this.getScaledWidth());
			y = (int) MathHelper.clamp(mouseY - heldFromY, 0, MinecraftClient.getInstance().getWindow().getScaledHeight() - this.getScaledHeight());
		}
		return true;
	}

	public int getScaledWidth() {
		return (int) (width * scale);
	}

	public int getScaledHeight() {
		return (int) (height * scale);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + this.getScaledWidth() && mouseY >= y && mouseY <= y + this.getScaledHeight();
	}

	public void renderBorder(DrawContext context) {
		context.drawBorder(x, y, this.getScaledWidth(), this.getScaledHeight(), state.color);
	}

	/**
	 * For rendering the widget in the HUD.
	 */
	public abstract void renderNormalWidget(DrawContext context, float delta);

	/**
	 * For rendering the widget in the config screen.
	 */
	public void renderConfigWidget(DrawContext context, float delta) {
		renderNormalWidget(context, delta);
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

	public void renderScale(DrawContext context) {
		context.drawTexture(SCALE_TEXTURE, x + getScaledWidth() - 8, y + getScaledHeight() - 8, 0, 0f, 0f, 8, 8, 8, 8);
	}

	public enum State {
		SELECTED(0xFFFFFFFF),
		HOVERED(0xAACCCCCC),
		INACTIVE(0x99999999);

		final int color;

		State(int color) {
			this.color = color;
		}
	}
}
