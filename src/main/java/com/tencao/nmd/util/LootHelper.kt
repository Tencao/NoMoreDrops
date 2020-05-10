package com.tencao.nmd.util

import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.party.IParty
import be.bluexin.saomclib.party.IPartyData
import com.tencao.nmd.LootSettingsEnum
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.data.ServerLootObject
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.network.packets.LootClientPKT
import com.tencao.nmd.registry.LootRegistry
import com.tencao.nmd.registry.LootTableMapper
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.fml.common.FMLCommonHandler
import java.util.*

object LootHelper {

    fun sortLoot(entityItem: EntityItem, player: UUID){
        sortLoot(SimpleEntityItem(entityItem), player)
    }

    /**
     * Single player, no party
     */
    fun sortLoot(entityItem: SimpleEntityItem, player: UUID){
        if (NMDConfig.lootcfg.alwaysDropMobLoot || checkPlayerWithinDistance(entityItem, listOf(player)))
            entityItem.spawnEntityPartyItem(player, false)
        else
            if (!PlayerHelper.addDropsToPlayer(player, entityItem.toStack(), false))
                entityItem.spawnEntityPartyItem(player, false)
    }


    fun sortLoot(entityItem: EntityItem, players: List<UUID>){
        sortLoot(SimpleEntityItem(entityItem), players)
    }

    /**
     * Multi-player, party
     */
    fun sortLoot(entityItem: SimpleEntityItem, players: List<UUID>){
        var party: IPartyData? = null
        players.forEach {
            val playerParty = PlayerHelper.getParty(it)
            if (playerParty != null) {
                party = playerParty
                return@forEach
            }
        }

        val leaderNMD = party?.leaderInfo?.player?.getNMDData()
        val rarity = LootTableMapper.getRarity(entityItem.toStack())
        val lootSetting = leaderNMD?.getLootSetting(rarity)?: LootSettingsEnum.None

        // If no players are in range or mob loot is always set to drop,
        // drop loot and continue party distribution later.
        if (NMDConfig.lootcfg.alwaysDropMobLoot || !checkPlayerWithinDistance(entityItem, players))
            entityItem.spawnEntityPartyItem(party!!, false)
        else {
            processLoot(entityItem, players, lootSetting, rarity)
        }
    }

    /**
     * Used when loot has already been dropped and need fully processing
     */
    fun handleLoot(entityItem: SimpleEntityItem, players: List<UUID>) {
        var party: IPartyData? = null
        players.forEach {
            val playerParty = PlayerHelper.getParty(it)
            if (playerParty != null) {
                party = playerParty
                return@forEach
            }
        }

        val leaderNMD = party?.leaderInfo?.player?.getNMDData()
        val rarity = LootTableMapper.getRarity(entityItem.toStack())
        val lootSetting = leaderNMD?.getLootSetting(rarity) ?: LootSettingsEnum.None
        processLoot(entityItem, players, lootSetting, rarity)
    }

    /**
     * Used to distribute loot between multiple players
     */
    private fun processLoot(entityItem: SimpleEntityItem, players: List<UUID>, lootSetting: ILootSettings, rarity: IRarity){
        if (lootSetting is ISpecialLootSettings) {
            //We're adding an additional 20 ticks to compensate for lag during packet sending
            val rollID = UUID.randomUUID()
            val time: Long = FMLCommonHandler.instance().minecraftServerInstance.getWorld(0).totalWorldTime + (NMDConfig.partycfg.LootRollTimer * 20).toLong() + 20L
            val serverLootObject = ServerLootObject(entityItem, players, time, rollID, rarity, lootSetting, LootRegistry.getServerLootCache(lootSetting, players))
            LootRegistry.lootdrops.add(serverLootObject)
            players.forEach {
                PlayerHelper.getPlayer(it)?.let { player ->
                    if (serverLootObject.shouldSendPlayer(player))
                        PacketPipeline.sendTo(LootClientPKT(entityItem.simpleStack, NMDConfig.partycfg.LootRollTimer * 20, rollID, rarity, lootSetting), player)
                }
            }
        }
        else {
            lootSetting.handleLoot(entityItem, players, LootRegistry.getServerLootCache(lootSetting, players))?.let { cache ->
                LootRegistry.updateServerCache(lootSetting, players, cache)
            }
        }
    }

    fun checkPlayerWithinDistance(entityItem: SimpleEntityItem, players: List<UUID>): Boolean {
        players.forEach {
            if (PlayerHelper.getPlayer(it)?.getDistanceSq(entityItem.pos)?: Double.MAX_VALUE <= PlayerHelper.squareSum(NMDConfig.lootcfg.distanceForDrop.toDouble()))
                return true
        }
        return false
    }


}