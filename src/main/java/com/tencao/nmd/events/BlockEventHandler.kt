package com.tencao.nmd.events

import com.tencao.nmd.util.PlayerHelper
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class BlockEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    fun onBlockDrops(e: BlockEvent.HarvestDropsEvent) {
        if (!e.world.isRemote && PlayerHelper.isPlayer(e.harvester)) {
            e.drops.removeAll{PlayerHelper.addDropsToPlayer(e.harvester, it, false) && it.isEmpty}
            e.drops.removeAll{PlayerHelper.addDropsToParty(e.harvester, it, false) && it.isEmpty}
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockBreak(e: BlockEvent.BreakEvent){
        if (!e.world.isRemote) {
            val tile = e.world.getTileEntity(e.pos)
            val player = e.player
            if (PlayerHelper.isPlayer(player)) {
                if (tile is IInventory) {
                    val inventory = tile as IInventory
                    for (i in 0 until inventory.sizeInventory) {
                        val itemStack = inventory.getStackInSlot(i)
                        if (!itemStack.isEmpty)
                            if (PlayerHelper.addDropsToPlayer(player, itemStack, true) && itemStack.isEmpty)
                                inventory.setInventorySlotContents(i, ItemStack.EMPTY)
                    }
                }
                player.addExperience(e.expToDrop)
                e.expToDrop = 0
            }

        }
    }

}