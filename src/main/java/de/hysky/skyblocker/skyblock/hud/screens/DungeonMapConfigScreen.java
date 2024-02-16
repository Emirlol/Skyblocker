package de.hysky.skyblocker.skyblock.hud.screens;

import de.hysky.skyblocker.skyblock.hud.elements.DungeonMapElement;
import de.hysky.skyblocker.skyblock.hud.elements.DungeonScoreElement;
import net.minecraft.client.gui.screen.Screen;

public class DungeonMapConfigScreen extends UIElementConfigScreen {
	public DungeonMapConfigScreen(Screen parent) {
		super(parent);
		addUIElement(DungeonMapElement.getInstance());
		addUIElement(DungeonScoreElement.getInstance());
	}
}
