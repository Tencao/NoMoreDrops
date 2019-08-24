package com.tencao.nmd.events.listener

import be.bluexin.saomclib.capabilities.PartyCapability
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.events.PartyEventV2
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.party.IPlayerInfo
import be.bluexin.saomclib.party.PlayerInfo
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.DropRarityEnum
import com.tencao.nmd.LootSettingsEnum
import com.tencao.nmd.SpecialLootSettingsEnum
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.events.listener.LivingDeathListener.cache
import com.tencao.nmd.util.PlayerHelper
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.events.handler.LootDropEvent
import com.tencao.nmd.events.handler.PartyLootEvent
import com.tencao.nmd.events.handler.RegisterLootRarityEvent
import com.tencao.nmd.network.packets.LootSyncAllPKT
import com.tencao.nmd.registry.LootRegistry
import com.tencao.nmd.registry.LootTableMapper
import com.tencao.nmd.util.PartyHelper
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

object DropEventListener {


    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingDrop(event: LivingDropsEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote && cache.containsKey(mob.uniqueID)){
            val player = cache[mob.uniqueID].first()
            val party = player.getPartyCapability().party
            if (PartyHelper.isValidParty(party)) {
                if (NMDConfig.lootcfg.alwaysDropMobLoot){
                    event.drops.removeIf {entityItem ->
                        val entity = SimpleEntityItem(entityItem)
                        val rarity = LootTableMapper.getRarity(entityItem.item)
                        MinecraftForge.EVENT_BUS.post(LootDropEvent(entity, party?.leaderInfo?.player?: player, rarity, LootSettingsEnum.None, true))
                        true
                    }
                }
                else {
                    val leaderNMDData = party?.leaderInfo?.player?.getNMDData()?: player.getNMDData()
                    event.drops.removeIf { entityItem ->
                        val entity = SimpleEntityItem(entityItem)
                        val rarity = LootTableMapper.getRarity(entityItem.item)
                        if (NMDConfig.lootcfg.lootModule && player.getDistanceSq(event.entityLiving) <= PlayerHelper.squareSum(NMDConfig.lootcfg.distanceForDrop.toDouble())) {
                            MinecraftForge.EVENT_BUS.post(LootDropEvent(entity, player, rarity, leaderNMDData.getLootSetting(rarity), false))
                            true
                        } else {
                            MinecraftForge.EVENT_BUS.post(LootDropEvent(entity, player, rarity, leaderNMDData.getLootSetting(rarity), true))
                            true
                        }

                    }
                }
            } else {
                event.drops.removeIf { entityItem ->
                    val rarity = LootTableMapper.getRarity(entityItem.item)
                    MinecraftForge.EVENT_BUS.post(LootDropEvent(SimpleEntityItem(entityItem), player, rarity, LootSettingsEnum.None, NMDConfig.lootcfg.alwaysDropMobLoot || player.getDistanceSq(event.entityLiving) > PlayerHelper.squareSum(NMDConfig.lootcfg.distanceForDrop.toDouble())))
                    true
                }
            }

            event.isCanceled = event.drops.isEmpty()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingExpDrop(event: LivingExperienceDropEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote && cache.containsKey(mob.uniqueID)){
            cache[mob.uniqueID].forEach { player ->
                val party = player.getCapability(PartyCapability.CAP_INSTANCE, null)!!.getOrCreatePT()
                if (PartyHelper.isValidParty(party)) {
                    PartyHelper.addExpToParty(player, event.droppedExperience / cache[mob.uniqueID].size)
                } else {
                    player.addExperience(event.droppedExperience / cache[mob.uniqueID].size)
                }
            }
            event.droppedExperience = 0
            event.isCanceled = true
            cache.removeAll(mob.uniqueID)
        }
    }

}


object LootRegisterListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun registerLootRarity(event: RegisterLootRarityEvent){
        event.registerLootRarity(DropRarityEnum.UNKNOWN, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.COMMON, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.UNCOMMON, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.RARE, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.EPIC, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootSettings(LootSettingsEnum.Random)
        event.registerLootSettings(LootSettingsEnum.RoundRobin)

        LootRegistry.registerDefaults(event.getLootSettings(), event.getLootOptions(), event.getRarities())
        event.isCanceled = true
    }
}

object PartyEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLootDrop(event: LootDropEvent){
        val party = event.player.getPartyCapability().party
        if (PartyHelper.isValidParty(party) && event.lootSettings != LootSettingsEnum.None) {
            if (event.isDrop)
                event.entityItem.spawnEntityPartyItem(party!!, !event.isDrop)
            else {
                val leaderNMDData = party!!.leaderInfo!!.player!!.getNMDData()
                val lootSetting = leaderNMDData.getLootSetting(event.dropRarity)
                MinecraftForge.EVENT_BUS.post(PartyLootEvent(event.entityItem, party, lootSetting, event.dropRarity))
            }
        }
        else {
            if (event.isDrop)
                event.entityItem.spawnEntityPartyItem(event.player, false)
            else
                PlayerHelper.addDropsToPlayer(event.player, event.entityItem.toStack(), false)
        }
        event.isCanceled = true
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyJoin(event: PartyEventV2.Join){
        event.player.player?.getNMDData()?.setLootSetting(event.party!!.leaderInfo?.player!!.getNMDData().lootSettings)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyLeave(event: PartyEventV2.Leave){
        event.player.player?.getNMDData()?.resetLootSettings()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyDisband(event: PartyEventV2.Disbanded){
        event.party?.membersInfo?.mapNotNull(IPlayerInfo::player)?.forEach { it.getNMDData().resetLootSettings() }
        LootRegistry.removeServerLootCache(event.party!!)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyLoot(event: PartyLootEvent){
        if (event.lootSetting is ISpecialLootSettings)
            PartyHelper.sendLootPacket(event.entityItem, event.party, event.dropRarity, event.lootSetting as ISpecialLootSettings, event.rollID)
        else {
            event.lootSetting.handleLoot(event.entityItem, event.party, LootRegistry.getServerLootCache(event.lootSetting, event.party))?.let { cache ->
                LootRegistry.updateServerCache(event.lootSetting, event.party, cache)
            }
        }
        event.isCanceled = true
    }
}

object PlayerEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPlayerTick(event: TickEvent.PlayerTickEvent){
        if (event.phase == TickEvent.Phase.END) {
            event.player.getNMDData().tickLoot()
            BlockEventListener.explosionCache.removeIf {
                it.timer-- <= 0 || it.blocks.isEmpty()
            }
        }
    }

    fun onPlayerConnect(event: PlayerEvent.PlayerLoggedInEvent){
        PacketPipeline.sendTo(LootSyncAllPKT(event.player.getNMDData().lootSettings), event.player as EntityPlayerMP)
    }
}

object WorldEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldTick(event: TickEvent.WorldTickEvent){
        if (event.phase == TickEvent.Phase.END)
            if (!event.world.isRemote) {
                if (event.world.provider.dimension == 0) {
                    LootRegistry.lootdrops.asSequence().filter { ++it.tickTime <= event.world.totalWorldTime }.forEach { it.handleLoot() }
                }
            }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldLoad(event: WorldEvent.Load){
        if (event.world.provider.dimension == 0)
            LootTableMapper.generateCache(event.world)
    }
}