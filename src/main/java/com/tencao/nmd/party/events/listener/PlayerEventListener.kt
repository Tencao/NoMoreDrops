package com.tencao.nmd.party.events.listener

import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.core.capability.getNMDData
import com.tencao.nmd.party.network.packets.LootSyncAllPKT
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object PlayerEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPlayerTick(event: TickEvent.PlayerTickEvent){
        if (event.phase == TickEvent.Phase.END) {
            event.player.getNMDData().tickLoot()
        }
    }

    fun onPlayerConnect(event: PlayerEvent.PlayerLoggedInEvent){
        PacketPipeline.sendTo(LootSyncAllPKT(event.player.getNMDData().lootSettings), event.player as EntityPlayerMP)
    }
}