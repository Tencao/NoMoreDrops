package com.tencao.nmd.data

import com.tencao.nmd.api.IRarity
import com.tencao.nmd.api.ISpecialLootSettings
import java.util.*

/**
 * This is a loot drop specifically for clients where tick time is expected to be below Int.MAX
 * @param simpleStack The stack being rolled on
 * @param rollID The unique identifier for the roll
 * @param tickTime The time the loot expires
 * @param rarity The rarity of the item dropped
 * @param clientCache The cache for the loot type
 */
data class ClientLootObject(val simpleStack: SimpleStack, val rollID: UUID, var tickTime: Int, val rarity: IRarity, val lootSetting: ISpecialLootSettings, var clientCache: Any?){

    // Cached render position, recalculated everytime an element is added or removed
    var x: Int = 0
    var y: Int = 0

    fun setXY(x: Int, y: Int){
        this.x = x
        this.y = y
    }
}