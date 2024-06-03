package de.hysky.skyblocker.utils

import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

object Boxes {
	/** Returns the vector of the min pos of this box.  */
	fun Box.getMinVec() = Vec3d(minX, minY, minZ)


	/** Returns the vector of the max pos of this box.  */
	fun Box.getMaxVec() = Vec3d(maxX, maxY, maxZ)

	/** Returns the vector of the side lengths of this box.  */
	fun Box.getLengthVec() = Vec3d(lengthX, lengthY, lengthZ)


	/** Offsets this box so that minX, minY and minZ are all zero.  */
	fun Box.moveToZero() = offset(getMinVec().negate())

	/** Returns the distance between to opposite corners of the box.  */
	fun Box.getCornerLength() = getMinVec().distanceTo(getMaxVec())


	/** Returns the length of an axis in the box.  */
	fun Box.getAxisLength(axis: Direction.Axis) = getMax(axis) - getMin(axis)


	/** Returns a box with each axis multiplied by the amount specified.  */
	fun Box.multiply(amount: Double) = multiply(amount, amount, amount)

	/** Returns a box with each axis multiplied by the amount specified.  */
	fun Box.multiply(x: Double, y: Double, z: Double): Box = expand(
		getAxisLength(Direction.Axis.X) * (x - 1) / 2.0,
		getAxisLength(Direction.Axis.Y) * (y - 1) / 2.0,
		getAxisLength(Direction.Axis.Z) * (z - 1) / 2.0
	)
}
