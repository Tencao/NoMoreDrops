package com.tencao.nmd.events.listener

import be.bluexin.saomclib.capabilities.getBlockRecords
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.party.playerInfo
import com.tencao.nmd.data.ExplosionData
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.util.LootHelper
import com.tencao.nmd.util.PartyHelper
import com.tencao.nmd.util.PlayerHelper
import com.tencao.nmd.util.getCombatEntries
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BlockEventListener {
    val  explosionCache = mutableListOf<ExplosionData>()

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    fun onBlockDrops(e: BlockEvent.HarvestDropsEvent) {
        if (!e.world.isRemote && PlayerHelper.isPlayer(e.harvester)) {
            e.drops.removeAll{ PlayerHelper.addDropsToPlayer(e.harvester, it, false) && it.isEmpty}
            if (!e.world.getChunk(e.pos).getBlockRecords().isBlockModified(e.pos)) {
                val party = e.harvester.getPartyCapability().partyData
                if (PartyHelper.isValidParty(party)) {
                    e.drops.forEach { stack ->
                        LootHelper.sortLoot(SimpleEntityItem(stack, e.pos, e.world), party!!.getMembers().filter { it == e.harvester.playerInfo() })
                    }
                    e.drops.clear()
                }
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onExplosionEvent(e: ExplosionEvent){
        if (!e.world.isRemote){
            val entity = e.explosion.explosivePlacedBy
            var player: EntityPlayer? = null
            if (entity is EntityPlayer) player = entity
            else if (entity != null)
                player = entity.combatTracker.getCombatEntries().asSequence()
                        .filter{ PlayerHelper.isPlayer(it.damageSrc.trueSource)}
                        .map { it.damageSrc.trueSource as EntityPlayer }.firstOrNull()

            explosionCache.add(ExplosionData(player, e.explosion.affectedBlockPositions.toMutableList()))
        }
    }

}