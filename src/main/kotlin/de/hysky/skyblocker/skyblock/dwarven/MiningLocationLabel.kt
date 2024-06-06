package de.hysky.skyblocker.skyblock.dwarven

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.utils.render.RenderHelper.renderText
import de.hysky.skyblocker.utils.render.Renderable
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

data class MiningLocationLabel(val category: Category, val centerPos: Vec3d) : Renderable {
	constructor(category: Category, pos: BlockPos) : this(category, pos.toCenterPos())

	private val name: Text
		get() = if (SkyblockerConfigManager.config.mining.commissionWaypoints.useColor) Text.literal(category.categoryName).withColor(category.color)
				else Text.literal(category.categoryName)

	/**
	 * Renders the name and distance to the label scaled so can be seen at a distance
	 * @param context render context
	 */
	override fun render(context: WorldRenderContext) {
		val posUp = centerPos.add(0.0, 1.0, 0.0)
		val distance = context.camera().pos.distanceTo(centerPos)
		val scale = (SkyblockerConfigManager.config.mining.commissionWaypoints.textScale * (distance / 10)).toFloat()
		renderText(context, name, posUp, scale, true)
		renderText(context, Text.literal(Math.round(distance).toString() + "m").formatted(Formatting.YELLOW), posUp, scale, (MinecraftClient.getInstance().textRenderer.fontHeight + 1).toFloat(), true)
	}

	interface Category {
		//Avoid conflict with Enum.name()
		val categoryName: String

		//all the color codes are the color of the block the waypoint is for
		val color: Int
	}

	internal enum class DwarvenCategory(override val categoryName: String, val location: BlockPos) : Category {
		LAVA_SPRINGS("Lava Springs", BlockPos(60, 197, -15)),
		CLIFFSIDE_VEINS("Cliffside Veins", BlockPos(40, 128, 40)),
		RAMPARTS_QUARRY("Rampart's Quarry", BlockPos(-100, 150, -20)),
		UPPER_MINES("Upper Mines", BlockPos(-130, 174, -50)),
		ROYAL_MINES("Royal Mines", BlockPos(130, 154, 30)),
		GLACITE_WALKER("Glacite Walker", BlockPos(0, 128, 150));

		var isTitanium: Boolean = false

		override fun toString() = categoryName

		override val color: Int
			get() = if (isTitanium) 0xd8d6d8 else 0x45bde0
	}

	internal enum class DwarvenEmissaries(val location: BlockPos) : Category {
		LAVA_SPRINGS(BlockPos(58, 198, -8)),
		CLIFFSIDE_VEINS(BlockPos(42, 134, 22)),
		RAMPARTS_QUARRY(BlockPos(-72, 153, -10)),
		UPPER_MINES(BlockPos(-132, 174, -50)),
		ROYAL_MINES(BlockPos(171, 150, 31)),
		DWARVEN_VILLAGE(BlockPos(-37, 200, -92)),
		DWARVEN_MINES(BlockPos(89, 198, -92));

		override fun toString() = categoryName

		override val categoryName: String
			get() = "Emissary"

		override val color: Int
			get() = 0xffffff
	}

	internal enum class GlaciteCategory(override val categoryName: String, override val color: Int, val locations: Array<BlockPos>) : Category {
		AQUAMARINE("Aquamarine", 0x334cb1, arrayOf(BlockPos(-1, 139, 437), BlockPos(90, 151, 229), BlockPos(56, 151, 400), BlockPos(51, 117, 303))),
		ONYX("Onyx", 0x191919, arrayOf(BlockPos(79, 119, 411), BlockPos(-14, 132, 386), BlockPos(18, 136, 370), BlockPos(16, 138, 411), BlockPos(-68, 130, 408))),
		PERIDOT("Peridot", 0x667f33, arrayOf(BlockPos(-61, 147, 302), BlockPos(91, 122, 397), BlockPos(-73, 122, 458), BlockPos(-77, 120, 282))),
		CITRINE("Citrine", 0x664c33, arrayOf(BlockPos(-104, 144, 244), BlockPos(39, 119, 386), BlockPos(-57, 144, 421), BlockPos(-47, 126, 418))),
		CAMPFIRE("Base Camp", 0x983333, arrayOf(BlockPos(-7, 126, 229)));

		override fun toString() = categoryName
	}
}
