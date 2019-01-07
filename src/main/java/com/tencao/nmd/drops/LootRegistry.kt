package com.tencao.nmd.drops

import be.bluexin.saomclib.party.IParty
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.data.ServerLootObject

object LootRegistry {

    val lootdrops = mutableListOf<ServerLootObject>()

    private lateinit var registeredLootSettings: ImmutableList<ILootSettings>
    private lateinit var registeredRarity: ImmutableList<IRarity>
    lateinit var defaultLootPairings: ImmutableMap<IRarity,ILootSettings>
    private val serverLootCache: LinkedHashMap<ILootSettings, Pair<IParty, Any?>> = linkedMapOf()

    fun getRegisteredLoot(name: String): ILootSettings{
        return registeredLootSettings.first { it.toString().equals(name, true) }
    }

    fun getRegisteredRarity(name: String): IRarity{
        return registeredRarity.first { it.toString().equals(name, true) }
    }

    fun registerDefaults(lootsettings: LinkedHashMap<IRarity, ILootSettings>, lootOptions: HashSet<ILootSettings>, rarities: HashSet<IRarity>){
        registeredLootSettings = ImmutableList.copyOf(lootOptions)
        registeredRarity = ImmutableList.copyOf(rarities)
        defaultLootPairings = ImmutableMap.copyOf(lootsettings)
    }

    fun getServerLootCache(lootSettings: ILootSettings, party: IParty): Any? {
        return if (lootSettings.persistentCache()){
            var cache = serverLootCache[lootSettings]?.second
            if (cache == null){
                cache = lootSettings.createServerCache(party)
                serverLootCache[lootSettings] = Pair(party, cache)
            }
            cache
        }
        else lootSettings.createServerCache(party)
    }

    fun updateServerCache(lootSettings: ILootSettings, party: IParty, cache: Any){
        if (lootSettings.persistentCache())
            serverLootCache[lootSettings] = Pair(party, cache)
    }

    fun removeServerLootCache(party: IParty){
        serverLootCache.values.removeIf { it.first == party }
    }

    fun getNextLootSetting(lootSettings: ILootSettings): ILootSettings{
        var index = registeredLootSettings.indexOf(lootSettings)
        return if (++index >= registeredLootSettings.size) registeredLootSettings[0] else registeredLootSettings[index]
    }

}