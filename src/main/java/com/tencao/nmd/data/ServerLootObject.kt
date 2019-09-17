package com.tencao.nmd.data

import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.registry.LootRegistry
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
data class ServerLootObject(val entityItem: SimpleEntityItem, val party: Set<UUID>, val tickTime: Long, val rollID: UUID, val rarity: IRarity, val lootSetting: ISpecialLootSettings, val serverCache: Any?){

    fun areConditionsMet(): Boolean{
        return lootSetting.areConditionsMet(serverCache)
    }

    fun shouldSendPlayer(player: EntityPlayer): Boolean{
        return lootSetting.shouldSendToClient(player, serverCache)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerLootObject

        if (rollID != other.rollID) return false

        return true
    }

    override fun hashCode(): Int {
        return rollID.hashCode()
    }
}