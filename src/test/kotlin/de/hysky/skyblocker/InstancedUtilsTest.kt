package de.hysky.skyblocker

import de.hysky.skyblocker.utils.InstancedUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InstancedUtilsTest {
	@Test
	fun testSameInstanceEqual() {
		val vec1 = Vector3i(8, 8, 8)

		Assertions.assertEquals(vec1, vec1)
	}

	@Test
	fun testSameFieldValuesEqual() {
		val vec1 = Vector3i(8, 8, 8)
		val vec2 = Vector3i(8, 8, 8)

		Assertions.assertEquals(vec1, vec2)
	}

	@Test
	fun testDifferentFieldValuesEqual() {
		val vec1 = Vector3i(8, 8, 8)
		val vec2 = Vector3i(-8, -8, -8)

		Assertions.assertNotEquals(vec1, vec2)
	}

	@Test
	fun testHashCodeOfEqualFieldValues() {
		val vec1 = Vector3i(8, 8, 8)
		val vec2 = Vector3i(8, 8, 8)

		Assertions.assertEquals(vec1.hashCode(), vec2.hashCode())
	}

	@Test
	fun testHashCodeOfDifferentFieldValues() {
		val vec1 = Vector3i(8, 8, 8)
		val vec2 = Vector3i(-8, -8, -8)

		Assertions.assertNotEquals(vec1.hashCode(), vec2.hashCode())
	}

	@Test
	fun testToString() {
		val vec1 = Vector3i(8, 8, 8)

		Assertions.assertEquals(vec1.toString(), "Vector3i[x=8, y=8, z=8]")
	}

	@Suppress("unused")
	private class Vector3i(val x: Int, val y: Int, val z: Int) {
		override fun equals(o: Any?): Boolean {
			return try {
				InstancedUtils.equals(javaClass)!!.invokeExact(this, o) as Boolean
			} catch (ignored: Throwable) {
				super.equals(o)
			}
		}

		override fun hashCode(): Int {
			return try {
				InstancedUtils.hashCode(javaClass)!!.invokeExact(this) as Int
			} catch (ignored: Throwable) {
				System.identityHashCode(this)
			}
		}

		override fun toString(): String {
			return try {
				InstancedUtils.toString(javaClass)!!.invokeExact(this) as String
			} catch (ignored: Throwable) {
				super.toString()
			}
		}
	}
}