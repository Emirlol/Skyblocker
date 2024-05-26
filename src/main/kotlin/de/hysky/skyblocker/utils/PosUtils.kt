package de.hysky.skyblocker.utils

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos

object PosUtils {
    val ALT_BLOCK_POS_CODEC: Codec<BlockPos> = RecordCodecBuilder.create { instance ->
		instance.group(
			Codec.INT.fieldOf("x").forGetter { pos -> pos.x },
			Codec.INT.fieldOf("y").forGetter { pos -> pos.y },
			Codec.INT.fieldOf("z").forGetter { pos -> pos.z }
		).apply(instance) { i, j, k -> BlockPos(i, j, k) }
	}

    fun parsePosString(posData: String) = posData.split(',').let { BlockPos(it[0].toInt(), it[1].toInt(), it[2].toInt()) }

    fun BlockPos.toShortestString() = "$x,$y,$z"
}
