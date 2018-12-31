package com.tencao.nmd.drops

import be.bluexin.saomclib.party.IParty
import com.google.common.collect.ImmutableSet
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.data.ServerLootObject

object LootRegistry {

    val lootdrops = mutableListOf<ServerLootObject>()

    private lateinit var registeredLootSettings: ImmutableSet<Pair<ILootSettings, String>>
    private lateinit var registeredRarity: ImmutableSet<Pair<IRarity, String>>
    lateinit var defaultLootPairings: ImmutableSet<Pair<ILootSettings, IRarity>>
    private val serverLootCache: LinkedHashMap<ILootSettings, Pair<IParty, Any?>> = linkedMapOf()

    fun getRegisteredLoot(name: String): ILootSettings{
        return registeredLootSettings.first { it.second.equals(name, true) }.first
    }

    fun getRegisteredRarity(name: String): IRarity{
        return registeredRarity.first { it.second.equals(name, true) }.first
    }

    fun registerDefaults(list: List<Pair<ILootSettings, IRarity>>){
        val lootSettingBuilder = ImmutableSet.builder<Pair<ILootSettings, String>>()
        val rarityBuilder = ImmutableSet.builder<Pair<IRarity, String>>()
        val defaultLootPairingsBuilder = LinkedHashSet<Pair<ILootSettings, IRarity>>()
        list.forEachIndexed { _, pair ->
            lootSettingBuilder.add(Pair(pair.first, pair.first.toString()))
            rarityBuilder.add(Pair(pair.second, pair.second.toString()))
            defaultLootPairingsBuilder.asSequence().none { it.second == pair.second }.let { defaultLootPairingsBuilder.add(pair) }
        }
        registeredLootSettings = lootSettingBuilder.build()
        registeredRarity = rarityBuilder.build()
        defaultLootPairings = ImmutableSet.copyOf(defaultLootPairingsBuilder)
    }

    fun getServerLootCache(lootSettings: ILootSettings, party: IParty): Any? {
        if (lootSettings.persistentCache()){
            var cache = serverLootCache[lootSettings]?.second
            if (cache == null){
                cache = lootSettings.createServerCache(party)
                serverLootCache[lootSettings] = Pair(party, cache)
            }
            return cache
        }
        else return lootSettings.createServerCache(party)
    }

    fun updateServerCache(lootSettings: ILootSettings, party: IParty, cache: Any){
        if (lootSettings.persistentCache())
            serverLootCache[lootSettings] = Pair(party, cache)
    }

    fun removeServerLootCache(party: IParty){
        serverLootCache.values.removeIf { it.first == party }
    }

}