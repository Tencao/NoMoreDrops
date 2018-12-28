package com.tencao.nmd.events

import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.api.DropRarityEnum
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.api.LootSettingsEnum
import com.tencao.nmd.data.SimpleEntityItem
import net.minecraftforge.fml.common.eventhandler.Event
import java.util.*


class PartyLootEvent(val entityItem: SimpleEntityItem, val party: IParty, var lootSetting: ILootSettings, var dropRarity: IRarity, val rollID: UUID): Event()

class RegisterLootRarity(private val lootSettings: LinkedList<Pair<ILootSettings, IRarity>>): Event(){

    fun registerLootRarity(lootSetting: ILootSettings, rarity: IRarity){
        lootSettings.add(Pair(lootSetting, rarity))
    }

    fun registerRarity(rarity: IRarity){
        lootSettings.add(Pair(LootSettingsEnum.Random, rarity))
    }

    fun registerLootSettings(lootSetting: ILootSettings){
        lootSettings.add(Pair(lootSetting, DropRarityEnum.UNKNOWN))
    }

    fun getLootSettings(): List<Pair<ILootSettings, IRarity>>{
        return lootSettings.toList()
    }

}