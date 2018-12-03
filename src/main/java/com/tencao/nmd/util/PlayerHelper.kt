package com.tencao.nmd.util

import be.bluexin.saomclib.capabilities.PartyCapability
import be.bluexin.saomclib.capabilities.getPartyCapability
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
        if (!player.getNMDData().isBlackListed(entityItem.item)) {
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
            val partyMembers = party.members.filter { pl -> (!leader && party.leader != pl || leader) && !pl.getNMDData().isBlackListed(itemStack) && player.getDistanceSq(pl) <= squareSum(64)}.toMutableList()
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

    fun addExpToParty(player: EntityPlayer, exp: Int){
        val selectedMembers = player.getPartyCapability().party?.members?.filter { player.getDistanceSq(it) <= PlayerHelper.squareSum(128) } ?: sequenceOf(player)
        val givenExp = exp / selectedMembers.count()// Your version has an exp loss !!
        selectedMembers.forEach { it.addExperience(givenExp) }
    }

    /**
     * Inserts the given itemstack into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param entityItem  The entity item to insert
     */
    fun giveItemToPlayer(player: EntityPlayer, entityItem: EntityItem): Boolean {
        return giveItemToPlayer(player, entityItem.item)
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
            entityItem.item.count = stack.count - remainder.count
            val hook = net.minecraftforge.event.ForgeEventFactory.onItemPickup(entityItem, player)

            if (hook >= 0 && player.inventory.addItemStackToInventory(entityItem.item)) {
                player.onItemPickup(entityItem, entityItem.item.count)
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

    fun squareSum(number: Int): Int{
        return number * number
    }

    fun squareSum(number: Double): Double{
        return number * number
    }
}