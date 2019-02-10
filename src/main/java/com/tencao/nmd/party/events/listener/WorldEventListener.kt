package com.tencao.nmd.party.events.listener

import com.tencao.nmd.party.registry.LootTableMapper
import com.tencao.nmd.party.registry.LootRegistry
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object WorldEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldTick(event: TickEvent.WorldTickEvent){
        if (event.phase == TickEvent.Phase.END)
            if (!event.world.isRemote) {
                if (event.world.provider.dimension == 0) {
                    LootRegistry.lootdrops.asSequence().filter { ++it.tickTime <= event.world.totalWorldTime }.forEach { it.handleLoot() }
                }
            }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldLoad(event: WorldEvent.Load){
        if (event.world.provider.dimension == 0)
            LootTableMapper.generateCache(event.world)
    }
}