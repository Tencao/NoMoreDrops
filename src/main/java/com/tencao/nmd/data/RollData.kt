package com.tencao.nmd.data

import be.bluexin.saomclib.party.PlayerInfo
import be.bluexin.saomclib.party.playerInfo
import net.minecraft.entity.player.EntityPlayer
import java.util.*
import java.util.function.Predicate
import kotlin.random.Random

/**
 * The Roll Data for loot rolls
 * @param player The players info panel
 * @param roll The Loot Roll
 */
data class RollData(val playerInfo: PlayerInfo, var roll: Int = 0): Predicate<EntityPlayer> {

    fun roll(type: Int){
        when (type){
            1 -> greedRoll()
            2 -> needRoll()
            else -> passRoll()
        }

    }

    private fun needRoll(){
        roll = Random.nextInt(101, 200)
    }

    private fun greedRoll(){
        roll = Random.nextInt(1, 100)
    }

    private fun passRoll(){
        roll = -1
    }

    fun isRollValid(): Boolean{
        return roll > 0
    }

    fun hasRolled(): Boolean{
        return roll != 0
    }

    /**
     * Byte will return 0 if pass, 1 if greed, or 2 if need
     * Roll will be subtracted by 100 when roll is a need
     */
    fun getRoll(): Pair<Int, Byte>{
        return if (roll <= 0) Pair(-1, 0.toByte()) else if (roll <= 100) Pair(roll, 1.toByte()) else Pair(roll - 100, 2.toByte())
    }

    override fun test(player: EntityPlayer): Boolean {
        return playerInfo == player.playerInfo()
    }
}
