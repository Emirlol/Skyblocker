package de.hysky.skyblocker.config.datafixer

import com.mojang.datafixers.DSL
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.Typed
import com.mojang.datafixers.schemas.Schema
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Dynamic
import net.minecraft.util.Identifier

class ConfigFix2QuickNav(outputSchema: Schema?, changesType: Boolean) : ConfigDataFix(outputSchema, changesType) {
	override fun makeRule(): TypeRewriteRule {
		return fixTypeEverywhereTyped(
			"ConfigFix2QuickNav",
			inputSchema.getType(ConfigDataFixer.CONFIG_TYPE)
		) { typed: Typed<*> -> typed.update(DSL.remainderFinder()) { dynamic: Dynamic<*> -> this.fix(dynamic) } }
	}

	private fun <T> fix(dynamic: Dynamic<T?>): Dynamic<T?> {
		return fixVersion(dynamic).update(
			"quickNav"
		) { quickNav: Dynamic<*> ->
			quickNav
				.renameField("button12", "button13")
				.renameField("button11", "button12")
				.renameField("button10", "button11")
				.renameField("button9", "button10")
				.renameField("button8", "button9")
				.renameField("button7", "button8")
				.updateMapValues { button: Pair<Dynamic<*>, Dynamic<*>> -> if (button.first.asString().getOrThrow().startsWith("button")) button.mapSecond { button: Dynamic<*> -> this.fixButton(button) } else button }
		}
	}

	private fun <T> fixButton(button: Dynamic<T>): Dynamic<T> {
		return button.renameAndFixField("item", "itemData") { itemData: Dynamic<*> -> itemData.renameAndFixField("id", "item") { id: Dynamic<*> -> id.createString(Identifier(id.asString().getOrThrow()).toString()) } }
	}
}
