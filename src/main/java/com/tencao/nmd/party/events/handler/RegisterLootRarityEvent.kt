package com.tencao.nmd.party.events.handler

import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event
import java.util.HashSet
import java.util.LinkedHashMap

@Cancelable
class RegisterLootRarityEvent(private val lootSettings: LinkedHashMap<IRarity, ILootSettings>, private val lootOptions: HashSet<ILootSettings>, private val rarities: HashSet<IRarity>): Event(){

    constructor(): this(linkedMapOf<IRarity, ILootSettings>(), hashSetOf(), hashSetOf())

    fun registerLootRarity(rarity: IRarity, lootSetting: ILootSettings){
        lootSettings.putIfAbsent(rarity, lootSetting)
        lootOptions.add(lootSetting)
        rarities.add(rarity)
    }

    fun overrideLootRarity(rarity: IRarity, lootSetting: ILootSettings){
        lootSettings[rarity] = lootSetting
        lootOptions.add(lootSetting)
        rarities.add(rarity)
    }


    fun registerRarity(rarity: IRarity){
        rarities.add(rarity)
    }

    fun registerLootSettings(lootSetting: ILootSettings){
        lootOptions.add(lootSetting)
    }

    fun getLootSettings(): LinkedHashMap<IRarity, ILootSettings> {
        return lootSettings
    }

    fun getLootOptions(): HashSet<ILootSettings> {
        return lootOptions
    }

    fun getRarities(): HashSet<IRarity> {
        return rarities
    }
}