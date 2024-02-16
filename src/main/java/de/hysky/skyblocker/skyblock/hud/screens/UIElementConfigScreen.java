package de.hysky.skyblocker.skyblock.hud.screens;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.hud.elements.UIElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class UIElementConfigScreen extends Screen {
	//Elements are drawn based on their order in the list, so the order matters.
	private final List<UIElement> elements = new LinkedList<>();
	private final Screen parent;
	private @Nullable UIElement dragging;
	private @Nullable UIElement previousHovered;
	private @Nullable UIElement selected;

	protected UIElementConfigScreen(Screen parent) {
		super(Text.literal("UI Element Config"));
		this.parent = parent;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		for (UIElement element : elements) {
			element.renderConfigWidget(context, delta);
			element.renderBorder(context);
			element.renderScale(context);
		}
		//Draw this on top of everything, otherwise user might not know what to do if the text is obscured by the elements
		context.drawCenteredTextWithShadow(textRenderer, "Right click to revert changes", width >> 1, (height >> 1) - 5, Color.GRAY.getRGB());
		context.drawCenteredTextWithShadow(textRenderer, "Ctrl + right click to reset to defaults", width >> 1, (height >> 1) + 5, Color.GRAY.getRGB());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (selected != previousHovered) {
			if (selected != null) selected.setState(UIElement.State.INACTIVE);
			if (previousHovered != null) previousHovered.setState(UIElement.State.SELECTED);
			selected = previousHovered;
			if (selected != null) {
				elements.remove(selected);
				elements.add(selected);
			}
		}

		switch (button) {
			case GLFW.GLFW_MOUSE_BUTTON_1 -> { //Left click for dragging
				if (selected == null) return false;
				dragging = selected;
				return selected.onMouseClick(mouseX, mouseY, button);
			}
			case GLFW.GLFW_MOUSE_BUTTON_2 -> { //Right click for reverting/resetting location
				if (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
						|| GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS) {
					//Ctrl+Right click
					if (selected != null) selected.resetPosition();
					else elements.forEach(UIElement::resetPosition);
				} else {
					//Just right click
					if (selected != null) selected.revertPositionChange();
					else elements.forEach(UIElement::revertPositionChange);
				}
				return true;
			}
			default -> {
				return false;
			}
		}
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		UIElement hoveredElement = getHoveredElement(mouseX, mouseY);
		if (previousHovered != hoveredElement) {
			if (previousHovered != null && previousHovered.getState() != UIElement.State.SELECTED) previousHovered.setState(UIElement.State.INACTIVE);
			if (hoveredElement != null && hoveredElement.getState() != UIElement.State.SELECTED) hoveredElement.setState(UIElement.State.HOVERED);
			previousHovered = hoveredElement;
		}
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging == null) {
			dragging = previousHovered;
			if (dragging == null) return false;
		}
		return dragging.onMouseDrag(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (dragging != null) {
			UIElement temp = dragging;
			dragging = null;
			return temp.onMouseRelease(mouseX, mouseY, button);
		}
		return true;
	}

	@Override
	public void close() {
		for (UIElement element : elements) {
			element.saveToConfig();
		}
		SkyblockerConfigManager.save();
		this.client.setScreen(parent);
	}

	/**
	 * @return The element under the cursor.
	 * Will return the top-most element if multiple elements are under the cursor,
	 * or <code>null</code> if there is no hovered element.
	 */
	@Nullable
	public UIElement getHoveredElement(double mouseX, double mouseY) {
		UIElement found = null;
		for (UIElement element : elements) {
			if (element.isMouseOver(mouseX, mouseY)) found = element; //Do not make this return early, so that we can get the top-most element.
		}
		return found;
	}

	public void addUIElement(UIElement element) {
		elements.add(element);
	}
}
