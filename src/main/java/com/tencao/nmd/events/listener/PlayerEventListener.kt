package com.tencao.nmd.events.listener

import be.bluexin.saomclib.onServer
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.party.playerInfo
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.network.packets.LootClientPKT
import com.tencao.nmd.network.packets.LootSyncAllPKT
import com.tencao.nmd.registry.LootRegistry
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
            BlockEventListener.explosionCache.removeIf {
                it.timer-- <= 0 || it.blocks.isEmpty()
            }
        }
    }

    @SubscribeEvent
    fun onPlayerConnect(e: PlayerEvent.PlayerLoggedInEvent){
        PacketPipeline.sendTo(LootSyncAllPKT(e.player.getNMDData().lootSettings), e.player as EntityPlayerMP)

        e.player.world.onServer {
            LootRegistry.lootdrops.asIterable().forEach {
                if (it.party.contains(e.player.playerInfo()) && it.shouldSendPlayer(e.player)) {
                    PacketPipeline.sendTo(LootClientPKT(it.entityItem.simpleStack, (it.tickTime - e.player.world.totalWorldTime).toInt() - 20, it.rollID, it.rarity, it.lootSetting), e.player as EntityPlayerMP)
                }
            }
        }
    }
}
