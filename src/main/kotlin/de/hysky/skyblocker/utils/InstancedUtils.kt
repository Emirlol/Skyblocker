package de.hysky.skyblocker.utils

import com.mojang.logging.LogUtils
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.slf4j.Logger
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field
import java.lang.runtime.ObjectMethods
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

/**
 * @implNote If implementing any of these onto a class, ensure that all subclasses have an implementation of the methods too.
 */
object InstancedUtils {
	private val LOGGER: Logger = LogUtils.getLogger()
	private val EQUALS_CACHE: MutableMap<Class<*>, MethodHandle> = ConcurrentHashMap()
	private val HASH_CODE_CACHE: MutableMap<Class<*>, MethodHandle> = ConcurrentHashMap()
	private val TO_STRING_CACHE: MutableMap<Class<*>, MethodHandle> = ConcurrentHashMap()

	fun equals(type: Class<*>): MethodHandle? {
		if (EQUALS_CACHE.containsKey(type)) return EQUALS_CACHE[type]

		try {
			val fields = getClassFields(type)
			val getters = getFieldGetters(fields)

			//The field names param can be anything as equals and hashCode don't care about it.
			val equalsHandle = ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodHandle::class.java, type, "", *getters) as MethodHandle

			EQUALS_CACHE[type] = equalsHandle

			return equalsHandle
		} catch (t: Throwable) {
			LOGGER.error("[Skyblocked Instanced Utils] Failed to create an equals method handle.", t)

			throw RuntimeException()
		}
	}

	fun hashCode(type: Class<*>): MethodHandle? {
		if (HASH_CODE_CACHE.containsKey(type)) return HASH_CODE_CACHE[type]

		try {
			val fields = getClassFields(type)
			val getters = getFieldGetters(fields)

			//The field names param can be anything as equals and hashCode don't care about it.
			val hashCodeHandle = ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodHandle::class.java, type, "", *getters) as MethodHandle

			HASH_CODE_CACHE[type] = hashCodeHandle

			return hashCodeHandle
		} catch (t: Throwable) {
			LOGGER.error("[Skyblocked Instanced Utils] Failed to create a hashCode method handle.", t)

			throw RuntimeException()
		}
	}

	fun toString(type: Class<*>): MethodHandle? {
		if (TO_STRING_CACHE.containsKey(type)) return TO_STRING_CACHE[type]

		try {
			val fields = getClassFields(type)
			val getters = getFieldGetters(fields)
			val fieldNames = java.lang.String.join(";", *Arrays.stream<Field>(fields).map<String> { obj: Field -> obj.name }.toArray<String> { _Dummy_.__Array__() })

			val toStringHandle = ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodHandle::class.java, type, fieldNames, *getters) as MethodHandle

			TO_STRING_CACHE[type] = toStringHandle

			return toStringHandle
		} catch (t: Throwable) {
			LOGGER.error("[Skyblocked Instanced Utils] Failed to create a toString method handle.", t)

			throw RuntimeException()
		}
	}

	private fun getClassFields(type: Class<*>): Array<Field> {
		return Stream.concat<Field>(Arrays.stream<Field>(type.declaredFields), Arrays.stream<Field>(type.fields)).distinct().toArray<Field> { _Dummy_.__Array__() }
	}

	@Throws(Throwable::class)
	private fun getFieldGetters(fields: Array<Field>): Array<MethodHandle> {
		val handles = ObjectOpenHashSet<MethodHandle>()

		for (field in fields) {
			field.isAccessible = true

			val getter = MethodHandles.lookup().unreflectGetter(field)

			handles.add(getter)
		}

		return handles.toArray<MethodHandle> { _Dummy_.__Array__() }
	}
}
