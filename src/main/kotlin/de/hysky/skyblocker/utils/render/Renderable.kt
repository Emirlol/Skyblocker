package de.hysky.skyblocker.utils.render

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext

interface Renderable {
	fun render(context: WorldRenderContext?)
}
