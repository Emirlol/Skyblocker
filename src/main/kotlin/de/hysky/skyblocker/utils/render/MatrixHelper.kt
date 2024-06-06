package de.hysky.skyblocker.utils.render

import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f

/**
 * Matrix helper methods
 */
object MatrixHelper {
	/**
	 * Copies the `matrix` into a new [Matrix4f]. This is necessary otherwise
	 * any transformations applied will affect other uses of the same matrix.
	 */
	fun Matrix4f.copyOf() = Matrix4f(this)

	/**
	 * Creates a blank [MatrixStack] and sets it's position matrix to the supplied
	 * `positionMatrix`.
	 */
	fun Matrix4f.toStack() = MatrixStack().apply { peek().positionMatrix.set(this@toStack) }
}
