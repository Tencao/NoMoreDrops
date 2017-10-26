package com.tencao.nmd.events

import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.util.PlayerHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class BlockEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockDrops(e: BlockEvent.HarvestDropsEvent) {
        if (!e.world.isRemote && PlayerHelper.isPlayer(e.harvester)) {
            e.drops.removeAll { !(e.harvester as EntityPlayer).getNMDData().isBlackListed(it) && e.harvester.inventory.addItemStackToInventory(it) }
        }
    }

}