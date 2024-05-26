package de.hysky.skyblocker

import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.transformer.IMixinTransformer

class MixinsTest {
	@Test
	fun auditMixins() {
		//Ensure that the transformer is active so that the Mixins can be audited
		Assertions.assertInstanceOf(IMixinTransformer::class.java, MixinEnvironment.getCurrentEnvironment().activeTransformer)

		//If this fails check the report to get the full stack trace
		MixinEnvironment.getCurrentEnvironment().audit()
	}

	companion object {
		@BeforeAll
		fun setupEnvironment() {
			SharedConstants.createGameVersion()
			Bootstrap.initialize()
		}
	}
}
