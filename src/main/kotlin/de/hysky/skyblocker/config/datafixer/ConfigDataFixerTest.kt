package de.hysky.skyblocker.config.datafixer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import org.junit.jupiter.api.Assertions
import java.io.InputStreamReader

class ConfigDataFixerTest {
	@Test
	fun testDataFixer1() {
		val oldConfig = GSON.fromJson(InputStreamReader(ConfigDataFixerTest::class.java.getResourceAsStream("/assets/skyblocker/config/skyblocker-v1.json")), JsonObject::class.java)
		val expectedNewConfig = GSON.fromJson(InputStreamReader(ConfigDataFixerTest::class.java.getResourceAsStream("/assets/skyblocker/config/skyblocker-v2.json")), JsonObject::class.java)

		Assertions.assertEquals(expectedNewConfig, ConfigDataFixer.apply(oldConfig, 2))
	}

	@Test
	fun testDataFixer2QuickNav() {
		val oldConfig = GSON.fromJson(InputStreamReader(ConfigDataFixerTest::class.java.getResourceAsStream("/assets/skyblocker/config/skyblocker-v2.json")), JsonObject::class.java)
		val expectedNewConfig = GSON.fromJson(InputStreamReader(ConfigDataFixerTest::class.java.getResourceAsStream("/assets/skyblocker/config/skyblocker-v3.json")), JsonObject::class.java)

		Assertions.assertEquals(expectedNewConfig, apply(oldConfig))
	}

	companion object {
		private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

		@BeforeAll
		fun setupEnvironment() {
			SharedConstants.createGameVersion()
			Bootstrap.initialize()
		}
	}
}
