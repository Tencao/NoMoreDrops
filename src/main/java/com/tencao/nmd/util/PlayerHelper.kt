package com.tencao.nmd.util

import be.bluexin.saomclib.capabilities.PartyCapability
import com.tencao.nmd.capability.getNMDData
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.SoundCategory
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper

object PlayerHelper {

    /**
     * This makes sure the player is actually a player and not a fake player
     * @param player The object to check
     * *
     * @return Returns true if player
     */
    fun isPlayer(player: Any?): Boolean {
        return player is EntityPlayer && player !is FakePlayer
    }

    /**
     * @param player The player who issued the drops
     * @param itemStack The ItemStack to attempt to add
     * @param saveSlot If true, will attempt to save one free slot in the players inventory
     */
    fun addDropsToPlayer(player: EntityPlayer, itemStack: ItemStack, saveSlot: Boolean): Boolean {
        if (!player.getNMDData().isBlackListed(itemStack)) {
            return giveItemToPlayer(player, itemStack)
        }
        return false
    }

    /**
     * @param player The player who issued the drops
     * @param entityItem The EntityItem to attempt to add
     * @param saveSlot If true, will attempt to save one free slot in the players inventory
     */
    fun addDropsToPlayer(player: EntityPlayer, entityItem: EntityItem, saveSlot: Boolean): Boolean {
        if (!player.getNMDData().isBlackListed(entityItem.entityItem)) {
            return giveItemToPlayer(player, entityItem)
        }
        return false
    }

    /**
     * @param player The player who issued the drops
     * @param itemStack The ItemStack to attempt to add
     * @param leader If the leader should be included in the drops
     */
    fun addDropsToParty(player: EntityPlayer, itemStack: ItemStack, leader: Boolean): Boolean{
        val party = player.getCapability(PartyCapability.CAP_INSTANCE, null)!!.party
        if (party != null) {
            val partyMembers = party.members.filter { pl -> (!leader && party.leader != pl || leader) && !pl.getNMDData().isBlackListed(itemStack) && player.getDistanceToEntity(pl) <= 64}.toMutableList()
            if (partyMembers.isNotEmpty()) {
                while (partyMembers.isNotEmpty() && !itemStack.isEmpty){
                    val member = partyMembers[player.world.rand.nextInt(partyMembers.size - 1)]
                    if (giveItemToPlayer(member, ItemStack(itemStack.item, 1))){
                        itemStack.shrink(1)
                    }
                    else partyMembers.remove(member)
                }
                return true
            }
        }
        return false
    }


    /*
    private fun attemptPickUp(entityItem: EntityItem, player: EntityPlayer): Boolean {
        if (entityItem.isDead || entityItem.entityItem.isEmpty){
            NMDCore.LOGGER.fatal("Attempted to add null stack to inventory")
            return true
        }

        val hook = net.minecraftforge.event.ForgeEventFactory.onItemPickup(entityItem, player)
        if (hook >= 0) {
            if ((hook == 1 || player.inventory.addItemStackToInventory(entityItem.entityItem))) {
                //net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerItemPickupEvent(player, entityItem, entityItem.entityItem)
                player.onItemPickup(entityItem, entityItem.entityItem.count)

                entityItem.setDead()

                player.addStat(StatList.getObjectsPickedUpStats(entityItem.entityItem.item), entityItem.entityItem.count)
                return true
            }
        }
        entityItem.setDead()
        return false
    }*/

    /**
     * Inserts the given itemstack into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param entityItem  The entity item to insert
     */
    fun giveItemToPlayer(player: EntityPlayer, entityItem: EntityItem): Boolean {
        return giveItemToPlayer(player, entityItem.entityItem)
    }

    /**
     * Inserts the given ItemStack into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The ItemStack to insert
     * @return Returns true if the ItemStack was changed
     */
    fun giveItemToPlayer(player: EntityPlayer, stack: ItemStack): Boolean {
        val inventory = PlayerMainInvWrapper(player.inventory)
        val world = player.world

        // copy the stack
        var remainder = stack.copy()

        // then into the inventory in general
        if (!remainder.isEmpty) {
            remainder = ItemHandlerHelper.insertItemStacked(inventory, remainder, true)
        }

        // play sound if something got picked up
        if (remainder.isEmpty || remainder.count != stack.count) {

            val entityItem = EntityItem(player.world, player.posX, player.posY, player.posZ, stack)
            entityItem.entityItem.count = stack.count - remainder.count
            val hook = net.minecraftforge.event.ForgeEventFactory.onItemPickup(entityItem, player)

            if (hook >= 0 && player.inventory.addItemStackToInventory(entityItem.entityItem)) {
                player.onItemPickup(entityItem, entityItem.entityItem.count)
                entityItem.setDead()
                world.playSound(player, player.posX, player.posY, player.posZ,
                        SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7f + 1.0f) * 2.0f)
                player.addStat(StatList.getObjectsPickedUpStats(remainder.item), stack.count - remainder.count)
                stack.count = remainder.count

                return true
            }
            entityItem.setDead()
        }
        return false
    }

    fun canTakeItem(player: EntityPlayer, stack: ItemStack): Boolean {
        val inventory = PlayerMainInvWrapper(player.inventory)
        // try adding it into the inventory
        var remainder = stack

        // then into the inventory in general
        if (!remainder.isEmpty) {
            remainder = ItemHandlerHelper.insertItemStacked(inventory, remainder, true)
        }
        return remainder.isEmpty || remainder.count != stack.count
    }
}