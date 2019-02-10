package com.tencao.nmd.loot.events.listener

import be.bluexin.saomclib.capabilities.getPartyCapability
import com.tencao.nmd.core.util.PartyHelper
import com.tencao.nmd.core.util.PlayerHelper
import com.tencao.nmd.loot.events.handler.LootDropEvent
import com.tencao.nmd.party.DropRarityEnum
import com.tencao.nmd.party.data.SimpleEntityItem
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BlockEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    fun onBlockDrops(e: BlockEvent.HarvestDropsEvent) {
        if (!e.world.isRemote && PlayerHelper.isPlayer(e.harvester)) {
            e.drops.removeAll{ PlayerHelper.addDropsToPlayer(e.harvester, it, false) && it.isEmpty}
            val party = e.harvester.getPartyCapability().getOrCreatePT()
            if (PartyHelper.isValidParty(party)){
                e.drops.forEach { stack ->
                    MinecraftForge.EVENT_BUS.post(LootDropEvent(SimpleEntityItem(stack, e.pos, e.world), party, DropRarityEnum.UNKNOWN, true))
                }
                e.drops.clear()
            }
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