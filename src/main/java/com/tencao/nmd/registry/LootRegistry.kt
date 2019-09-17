package com.tencao.nmd.registry

import be.bluexin.saomclib.party.IParty
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.data.ServerLootObject
import java.util.*
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap

object LootRegistry {

    val lootdrops = mutableSetOf<ServerLootObject>()

    private lateinit var registeredLootSettings: ImmutableList<ILootSettings>
    private lateinit var registeredRarity: ImmutableList<IRarity>
    lateinit var defaultLootPairings: ImmutableMap<IRarity, ILootSettings>
    private val serverLootCache: LinkedHashMap<ILootSettings, Pair<Set<UUID>, Any?>> = linkedMapOf()

    fun getRegisteredLoot(name: String): ILootSettings {
        return registeredLootSettings.first { it.toString().equals(name, true) }
    }

    fun getRegisteredRarity(name: String): IRarity {
        return registeredRarity.first { it.toString().equals(name, true) }
    }

    fun registerDefaults(lootsettings: LinkedHashMap<IRarity, ILootSettings>, lootOptions: HashSet<ILootSettings>, rarities: HashSet<IRarity>){
        registeredLootSettings = ImmutableList.copyOf(lootOptions)
        registeredRarity = ImmutableList.copyOf(rarities)
        defaultLootPairings = ImmutableMap.copyOf(lootsettings)
    }

    fun getServerLootCache(lootSettings: ILootSettings, party: Set<UUID>): Any? {
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

    fun updateServerCache(lootSettings: ILootSettings, party: Set<UUID>, cache: Any){
        if (lootSettings.persistentCache())
            serverLootCache[lootSettings] = Pair(party, cache)
    }

    fun removeServerLootCache(party: IParty){
        serverLootCache.values.removeIf { it.first == party }
    }

    fun getNextLootSetting(lootSettings: ILootSettings): ILootSettings {
        var index = registeredLootSettings.indexOf(lootSettings)
        return if (++index >= registeredLootSettings.size) registeredLootSettings[0] else registeredLootSettings[index]
    }

}