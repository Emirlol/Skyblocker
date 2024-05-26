package de.hysky.skyblocker.utils.render

import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f

/**
 * Matrix helper methods
 */
interface MatrixHelper {
	companion object {
		/**
		 * Copies the `matrix` into a new [Matrix4f]. This is necessary otherwise
		 * any transformations applied will affect other uses of the same matrix.
		 */
		fun copyOf(matrix: Matrix4f?): Matrix4f? {
			return Matrix4f(matrix)
		}

		/**
		 * Creates a blank [MatrixStack] and sets it's position matrix to the supplied
		 * `positionMatrix`.
		 */
		fun toStack(positionMatrix: Matrix4f?): MatrixStack? {
			val matrices = MatrixStack()
			matrices.peek().positionMatrix.set(positionMatrix)

			return matrices
		}
	}
}
