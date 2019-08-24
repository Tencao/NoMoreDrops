package com.tencao.nmd.util

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.party.IParty
import be.bluexin.saomclib.party.IPlayerInfo
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.data.ServerLootObject
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.network.packets.LootClientPKT
import com.tencao.nmd.registry.LootRegistry
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.FMLCommonHandler
import java.util.*

object PartyHelper {

    fun isValidParty(party: IParty?): Boolean{
        return party is IParty && party.isParty
    }

    fun isValidParty(player: EntityPlayer?): Boolean{
        return player is EntityPlayer && isValidParty(player.getPartyCapability().getOrCreatePT())
    }

    /**
     * Processes the loot ready to be sent to
     * the client, as well as store a reference
     * on the server to process once all conditions
     * are met.
     */
    fun sendLootPacket(entityItem: SimpleEntityItem, party: IParty, rarity: IRarity, lootSettings: ISpecialLootSettings, rollID: UUID) {
        //We're adding an additional 20 ticks to compensate for lag during packet sending

        val time: Long = FMLCommonHandler.instance().minecraftServerInstance.getWorld(0).totalWorldTime + (NMDConfig.partycfg.LootRollTimer * 20).toLong() + 20L
        party.membersInfo.mapNotNull(IPlayerInfo::player).forEach {
            PacketPipeline.sendTo(LootClientPKT(entityItem.simpleStack, NMDConfig.partycfg.LootRollTimer * 20, rollID, rarity, lootSettings), it as EntityPlayerMP)
        }
        LootRegistry.lootdrops.add(ServerLootObject(entityItem, party, time, rollID, lootSettings, LootRegistry.getServerLootCache(lootSettings, party)))
    }

    fun addExpToParty(player: EntityPlayer, exp: Int){
        val selectedMembers = player.getPartyCapability().getOrCreatePT().membersInfo.mapNotNull(IPlayerInfo::player).filter { player.getDistanceSq(it) <= PlayerHelper.squareSum(128) }
        val givenExp = exp / selectedMembers.count()
        selectedMembers.forEach { it.addExperience(givenExp) }
    }
}