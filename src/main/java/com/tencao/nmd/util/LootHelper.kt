package com.tencao.nmd.util

import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.party.IPartyData
import be.bluexin.saomclib.party.PlayerInfo
import com.tencao.nmd.LootSettingsEnum
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.data.LootObject
import com.tencao.nmd.data.ServerLootObject
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.network.packets.LootClientPKT
import com.tencao.nmd.registry.LootRegistry
import com.tencao.nmd.registry.LootTableMapper
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.FMLCommonHandler
import java.util.*

object LootHelper {

    fun sortLoot(entityItem: EntityItem, player: PlayerInfo){
        sortLoot(SimpleEntityItem(entityItem), player)
    }

    /**
     * Single player, no party
     */
    fun sortLoot(entityItem: SimpleEntityItem, player: PlayerInfo){
        if (NMDConfig.lootcfg.alwaysDropMobLoot || checkPlayerWithinDistance(entityItem, listOf(player)))
            entityItem.spawnEntityPartyItem(player, false)
        else
            if (!PlayerHelper.addDropsToPlayer(player, entityItem.toStack(), false))
                entityItem.spawnEntityPartyItem(player, false)
    }


    fun sortLoot(entityItem: EntityItem, players: List<PlayerInfo>){
        sortLoot(SimpleEntityItem(entityItem), players)
    }

    /**
     * Multi-player, party
     */
    fun sortLoot(entityItem: SimpleEntityItem, players: List<PlayerInfo>){
        val rarity = LootTableMapper.getRarity(entityItem.toStack())
        sortLoot(entityItem, rarity, players)
    }

    /**
     * Multi-player, party
     */
    fun sortLoot(loot: LootObject, players: List<PlayerInfo>){
        sortLoot(loot.stack, loot.rarity, players)
    }

    fun sortLoot(entityItem: SimpleEntityItem, rarity: IRarity, players: List<PlayerInfo>){
        val party: IPartyData? = PlayerHelper.getParty(players.asSequence())

        val leaderNMD = party?.leaderInfo?.player?.getNMDData()
        val lootSetting = leaderNMD?.getLootSetting(rarity)?: LootSettingsEnum.None
        entityItem.rarity = rarity

        // If no players are in range or mob loot is always set to drop,
        // drop loot and continue party distribution later.
        if (NMDConfig.lootcfg.alwaysDropMobLoot || !checkPlayerWithinDistance(entityItem, players))
            if (party != null)
                entityItem.spawnEntityPartyItem(party, false)
            else
                entityItem.spawnEntityPartyItem(players, false)
        else {
            processLoot(entityItem, players, lootSetting)
        }
    }




    /**
     * Used when loot has already been dropped and need fully processing
     */
    fun handleLoot(entityItem: SimpleEntityItem, players: List<PlayerInfo>) {
        val party: IPartyData? = PlayerHelper.getParty(players.asSequence())

        val leaderNMD = party?.leaderInfo?.player?.getNMDData()
        entityItem.rarity = LootTableMapper.getRarity(entityItem.toStack())
        val lootSetting = leaderNMD?.getLootSetting(entityItem.rarity) ?: LootSettingsEnum.None
        processLoot(entityItem, players, lootSetting)
    }


    /**
     * Used to distribute loot between multiple players
     */
    private fun processLoot(entityItem: SimpleEntityItem, players: List<PlayerInfo>, lootSetting: ILootSettings){
        if (lootSetting is ISpecialLootSettings) {
            //We're adding an additional 20 ticks to compensate for lag during packet sending
            val rollID = UUID.randomUUID()
            val time: Long = FMLCommonHandler.instance().minecraftServerInstance.getWorld(0).totalWorldTime + (NMDConfig.partycfg.LootRollTimer * 20).toLong() + 20L
            val serverLootObject = ServerLootObject(entityItem, players, time, rollID, entityItem.rarity, lootSetting, LootRegistry.getServerLootCache(lootSetting, players))
            LootRegistry.lootdrops.add(serverLootObject)
            players.forEach { playerInfo ->
                playerInfo.player?.let { player ->
                    if (serverLootObject.shouldSendPlayer(player))
                        PacketPipeline.sendTo(LootClientPKT(entityItem.simpleStack, NMDConfig.partycfg.LootRollTimer * 20, rollID, entityItem.rarity, lootSetting), player as EntityPlayerMP)
                }
            }
        }
        else {
            lootSetting.handleLoot(entityItem, players, LootRegistry.getServerLootCache(lootSetting, players))?.let { cache ->
                LootRegistry.updateServerCache(lootSetting, players, cache)
            }
        }
    }

    fun checkPlayerWithinDistance(entityItem: SimpleEntityItem, players: List<PlayerInfo>): Boolean {
        players.forEach {
            if (it.player?.getDistanceSq(entityItem.pos)?: Double.MAX_VALUE <= PlayerHelper.squareSum(NMDConfig.lootcfg.distanceForDrop.toDouble()))
                return true
        }
        return false
    }


}