package com.tencao.nmd.util

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.party.IPartyData
import be.bluexin.saomclib.party.PlayerInfo
import net.minecraft.entity.player.EntityPlayer

object PartyHelper {

    fun isValidParty(party: IPartyData?): Boolean{
        return party is IPartyData && party.isParty
    }

    fun isValidParty(player: EntityPlayer?): Boolean{
        return player is EntityPlayer && isValidParty(player.getPartyCapability().partyData)
    }

    fun addExpToParty(player: EntityPlayer, exp: Int){
        val selectedMembers = player.getPartyCapability().partyData?.membersInfo?.mapNotNull(PlayerInfo::player)?.filter { player.getDistanceSq(it) <= PlayerHelper.squareSum(128) }?: listOf(player)
        val givenExp = exp / selectedMembers.count()
        selectedMembers.forEach { it.addExperience(givenExp) }
    }
}