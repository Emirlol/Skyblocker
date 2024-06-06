package de.hysky.skyblocker.compatibility

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class MixinPlugin : IMixinConfigPlugin {
	override fun onLoad(mixinPackage: String) {
		//Do nothing
	}

	override fun getRefMapperConfig(): String {
		return ""
	}

	override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
		//OptiFabric Compatibility
		return !mixinClassName.endsWith("WorldRendererMixin") || !OPTIFABRIC_LOADED
	}

	override fun acceptTargets(myTargets: Set<String>, otherTargets: Set<String>) {
		//Do nothing
	}

	override fun getMixins(): List<String> {
		returnemptyList()
	}

	override fun preApply(targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo) {
		//Do nothing
	}

	override fun postApply(targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo) {
		//Do nothing
	}

	companion object {
		private val OPTIFABRIC_LOADED = FabricLoader.getInstance().isModLoaded("optifabric")
	}
}
