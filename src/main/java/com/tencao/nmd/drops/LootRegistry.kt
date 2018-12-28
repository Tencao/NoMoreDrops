package com.tencao.nmd.drops

import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.data.ServerLootObject

object LootRegistry {

    val lootdrops = mutableListOf<ServerLootObject>()

    private var registeredLootSettings: LinkedHashSet<Pair<ILootSettings, String>> = linkedSetOf()
    private var registeredRarity: LinkedHashSet<Pair<IRarity, String>> = linkedSetOf()
    val defaultLootPairings: LinkedHashSet<Pair<ILootSettings, IRarity>> = linkedSetOf()

    fun getRegisteredLoot(name: String): ILootSettings{
        return registeredLootSettings.first { it.second.equals(name, true) }.first
    }

    fun getRegisteredRarity(name: String): IRarity{
        return registeredRarity.first { it.second.equals(name, true) }.first
    }

    fun registerDefaults(list: List<Pair<ILootSettings, IRarity>>){
        list.forEachIndexed { _, pair ->
            registeredLootSettings.add(Pair(pair.first, pair.first.toString()))
            registeredRarity.add(Pair(pair.second, pair.second.toString()))
            defaultLootPairings.asSequence().none { it.second == pair.second }.let { defaultLootPairings.add(pair) }
        }
    }

}