package de.hysky.skyblocker.utils

import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

object Boxes {
	/** Returns the vector of the min pos of this box.  */
	fun getMinVec(box: Box): Vec3d {
		return Vec3d(box.minX, box.minY, box.minZ)
	}

	/** Returns the vector of the max pos of this box.  */
	fun getMaxVec(box: Box): Vec3d {
		return Vec3d(box.maxX, box.maxY, box.maxZ)
	}

	/** Returns the vector of the side lengths of this box.  */
	fun getLengthVec(box: Box): Vec3d {
		return Vec3d(box.lengthX, box.lengthY, box.lengthZ)
	}

	/** Offsets this box so that minX, minY and minZ are all zero.  */
	fun moveToZero(box: Box): Box {
		return box.offset(getMinVec(box).negate())
	}

	/** Returns the distance between to oppisite corners of the box.  */
	fun getCornerLength(box: Box): Double {
		return getMinVec(box).distanceTo(getMaxVec(box))
	}

	/** Returns the length of an axis in the box.  */
	fun getAxisLength(box: Box, axis: Direction.Axis?): Double {
		return box.getMax(axis) - box.getMin(axis)
	}

	/** Returns a box with each axis multiplied by the amount specified.  */
	fun multiply(box: Box, amount: Double): Box {
		return multiply(box, amount, amount, amount)
	}

	/** Returns a box with each axis multiplied by the amount specified.  */
	fun multiply(box: Box, x: Double, y: Double, z: Double): Box {
		return box.expand(
			getAxisLength(box, Direction.Axis.X) * (x - 1) / 2.0,
			getAxisLength(box, Direction.Axis.Y) * (y - 1) / 2.0,
			getAxisLength(box, Direction.Axis.Z) * (z - 1) / 2.0
		)
	}
}
