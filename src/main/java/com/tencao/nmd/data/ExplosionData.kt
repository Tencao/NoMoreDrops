package com.tencao.nmd.data

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import java.util.function.Predicate

data class ExplosionData(val player: EntityPlayer?, val blocks: List<BlockPos>, var timer: Int = 5): Predicate<BlockPos> {

    override fun test(pos: BlockPos): Boolean {
        return blocks.contains(pos)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExplosionData

        if (player?.uniqueID != other.player?.uniqueID) return false
        if (blocks.size != other.blocks.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = player?.hashCode() ?: 0
        result = 31 * result + blocks.size.hashCode()
        return result
    }

}