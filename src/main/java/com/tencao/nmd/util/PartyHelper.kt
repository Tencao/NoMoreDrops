package com.tencao.nmd.util

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.drops.LootHolder
import com.tencao.nmd.drops.LootSettings
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

object PartyHelper {

    /**
     * @param player The player who issued the drops
     * @param itemStack The ItemStack to attempt to add
     * @param leader If the leader should be included in the drops
     */
    fun addDropsToParty(player: EntityPlayer, party: IParty, itemStack: ItemStack): Boolean{
        return when(player.getNMDData().lootSetting){
            LootSettings.Random -> addAsRandom(player, party, itemStack)
            LootSettings.RoundRobin -> addAsRoundRobin(player, party, itemStack)
            LootSettings.NeedBeforeGreed -> addAsNeedOrGreed(party, itemStack)
            LootSettings.MasterLooter -> addAsMasterLooter(party, itemStack)
        }
    }

    fun addAsRandom(player: EntityPlayer, party: IParty, itemStack: ItemStack): Boolean {
        val partyMembers = party.members.filter { pl -> !pl.getNMDData().isBlackListed(itemStack) && player.getDistanceSq(pl) <= PlayerHelper.squareSum(64) }.toMutableList()
        if (partyMembers.isNotEmpty()) {
            while (partyMembers.isNotEmpty() && !itemStack.isEmpty){
                val member = partyMembers[player.world.rand.nextInt(partyMembers.size - 1)]
                if (PlayerHelper.giveItemToPlayer(member, ItemStack(itemStack.item, 1))){
                    itemStack.shrink(1)
                }
                else partyMembers.remove(member)
            }
        }
        return itemStack.isEmpty
    }

    fun addAsRoundRobin(player: EntityPlayer, party: IParty, itemStack: ItemStack): Boolean {
        val partyMembers = party.members.filter { pl -> !pl.getNMDData().isBlackListed(itemStack) && player.getDistanceSq(pl) <= PlayerHelper.squareSum(64) }.toMutableList()
        if (partyMembers.isNotEmpty()) {
            var lastMember = player.getNMDData().lastMember.toInt()
            while (partyMembers.isNotEmpty() && !itemStack.isEmpty){
                if (lastMember >= partyMembers.size) lastMember = 0
                val member = partyMembers[lastMember]
                if (PlayerHelper.giveItemToPlayer(member, ItemStack(itemStack.item, 1))){
                    itemStack.shrink(1)
                    lastMember++
                }
                else partyMembers.remove(member)
            }
            player.getNMDData().lastMember = lastMember.toShort()
        }
        return itemStack.isEmpty
    }

    fun addAsNeedOrGreed(party: IParty, itemStack: ItemStack): Boolean {
        LootHolder.needOrGreed.add(Pair(itemStack.copy(), party.members.map(Entity::getUniqueID).toSortedSet()))
        return true
    }

    fun addAsMasterLooter(party: IParty, itemStack: ItemStack): Boolean {
        LootHolder.needOrGreed.add(Pair(itemStack.copy(), party.members.map(Entity::getUniqueID).toSortedSet()))
        return true
    }


    fun addExpToParty(player: EntityPlayer, exp: Int){
        val selectedMembers = player.getPartyCapability().party?.members?.filter { player.getDistanceSq(it) <= PlayerHelper.squareSum(128) } ?: sequenceOf(player)
        val givenExp = exp / selectedMembers.count()// Your version has an exp loss !!
        selectedMembers.forEach { it.addExperience(givenExp) }
    }
}