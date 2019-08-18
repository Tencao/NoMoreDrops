package com.tencao.nmd.party.events.listener

import be.bluexin.saomclib.events.PartyEventV2
import be.bluexin.saomclib.party.IPlayerInfo
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.core.capability.getNMDData
import com.tencao.nmd.core.util.PartyHelper
import com.tencao.nmd.loot.events.handler.LootDropEvent
import com.tencao.nmd.party.events.handler.PartyLootEvent
import com.tencao.nmd.party.registry.LootRegistry
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object PartyEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLootDrop(event: LootDropEvent){
        if (event.isDrop)
            event.entityItem.spawnEntityPartyItem(event.party, !event.isDrop)
        else {
            val leaderNMDData = event.party.leaderInfo?.player!!.getNMDData()
            val lootSetting = leaderNMDData.getLootSetting(event.dropRarity)
            MinecraftForge.EVENT_BUS.post(PartyLootEvent(event.entityItem, event.party, lootSetting, event.dropRarity, UUID.randomUUID()))
        }
        event.isCanceled = true
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyJoin(event: PartyEventV2.Join){
        event.player.player?.getNMDData()?.setLootSetting(event.party!!.leaderInfo?.player!!.getNMDData().lootSettings)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyLeave(event: PartyEventV2.Leave){
        event.player.player?.getNMDData()?.resetLootSettings()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyDisband(event: PartyEventV2.Disbanded){
        event.party?.membersInfo?.mapNotNull(IPlayerInfo::player)?.forEach { it.getNMDData().resetLootSettings() }
        LootRegistry.removeServerLootCache(event.party!!)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyLoot(event: PartyLootEvent){
        if (event.lootSetting is ISpecialLootSettings)
            PartyHelper.sendLootPacket(event.entityItem, event.party, event.dropRarity, event.lootSetting as ISpecialLootSettings, event.rollID)
        else {
            event.lootSetting.handleLoot(event.entityItem, event.party, LootRegistry.getServerLootCache(event.lootSetting, event.party))?.let { cache ->
                LootRegistry.updateServerCache(event.lootSetting, event.party, cache)
            }
        }
        event.isCanceled = true
    }
}