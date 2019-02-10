package com.tencao.nmd.party.data

import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.party.registry.LootRegistry
import net.minecraft.entity.player.EntityPlayer
import java.util.*

/**
 * This LootObject is designed for server side as the tickTime can hold the full world total tick
 * @param entityItem The stack being rolled on
 * @param tickTime The time the loot expires
 * @param rollID The unique identifier for the roll
 * @param lootSetting The Loot Settings used
 * @param serverCache The cache associated with the drop
 */
data class ServerLootObject(val entityItem: SimpleEntityItem, val party: IParty, var tickTime: Long, val rollID: UUID, val lootSetting: ISpecialLootSettings, val serverCache: Any?){

    fun areConditionsMet(): Boolean{
        return lootSetting.areConditionsMet(serverCache)
    }

    fun handleLoot(){
        lootSetting.handleLoot(entityItem, party, serverCache)?.let {cache ->
            LootRegistry.updateServerCache(lootSetting, party, cache)
        }
        LootRegistry.lootdrops.remove(this)
    }

    fun processClientData(player: EntityPlayer, clientCache: Any?){
        lootSetting.processClientCache(player, clientCache, serverCache)
        if (areConditionsMet()) {
            handleLoot()
        }
    }
}