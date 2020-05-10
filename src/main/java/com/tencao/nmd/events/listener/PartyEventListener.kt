package com.tencao.nmd.events.listener

import be.bluexin.saomclib.events.PartyEvent
import be.bluexin.saomclib.party.PlayerInfo
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.registry.LootRegistry
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PartyEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyJoin(event: PartyEvent.Join){
        event.player.player?.getNMDData()?.setLootSetting(event.partyData.leaderInfo.player!!.getNMDData().lootSettings)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyLeave(event: PartyEvent.Leave){
        event.player.player?.getNMDData()?.resetLootSettings()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyDisband(event: PartyEvent.Disbanded){
        event.partyData.membersInfo.mapNotNull(PlayerInfo::player).forEach { it.getNMDData().resetLootSettings() }
        LootRegistry.removeServerLootCache(event.partyData!!)
    }

}
