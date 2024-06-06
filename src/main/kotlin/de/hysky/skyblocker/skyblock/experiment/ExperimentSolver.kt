package de.hysky.skyblocker.skyblock.experiment

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.config.configs.HelperConfig.Experiments
import de.hysky.skyblocker.utils.render.gui.ContainerSolver
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterTick
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack

abstract class ExperimentSolver protected constructor(containerName: String?) : ContainerSolver(containerName) {
	enum class State {
		REMEMBER, WAIT, SHOW, END
	}

	@JvmField
	var state: State = State.REMEMBER
	private val slots: MutableMap<Int, ItemStack> = HashMap()

	fun getSlots(): Map<Int, ItemStack> {
		return slots
	}

	override val isEnabled: Boolean
		get() = isEnabled(SkyblockerConfigManager.config.helpers.experiments)

	protected abstract fun isEnabled(experimentsConfig: Experiments): Boolean

	protected override fun start(screen: GenericContainerScreen?) {
		super.start(screen)
		state = State.REMEMBER
		ScreenEvents.afterTick(screen).register(AfterTick { screen: Screen -> this.tick(screen) })
	}

	protected override fun reset() {
		super.reset()
		state = State.REMEMBER
		slots.clear()
	}

	protected abstract fun tick(screen: Screen)
}
