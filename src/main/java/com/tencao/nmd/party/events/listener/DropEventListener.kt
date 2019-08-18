package com.tencao.nmd.party.events.listener

import be.bluexin.saomclib.capabilities.PartyCapability
import be.bluexin.saomclib.capabilities.getPartyCapability
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.core.capability.getNMDData
import com.tencao.nmd.core.events.listener.LivingDeathListener.cache
import com.tencao.nmd.core.util.PartyHelper
import com.tencao.nmd.core.util.PlayerHelper
import com.tencao.nmd.loot.events.handler.LootDropEvent
import com.tencao.nmd.party.LootSettingsEnum
import com.tencao.nmd.party.data.SimpleEntityItem
import com.tencao.nmd.party.registry.LootTableMapper
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DropEventListener {

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onLivingDrop(event: LivingDropsEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote && cache.containsKey(mob.uniqueID)){
            val player = cache[mob.uniqueID].first()
            val party = player.getPartyCapability().getOrCreatePT()
            if (PartyHelper.isValidParty(party)) {
                val leaderNMDData = party.leaderInfo?.player!!.getNMDData()

                event.drops.forEach { entityItem ->
                    val rarity = LootTableMapper.getRarity(entityItem.item)
                    if (leaderNMDData.getLootSetting(rarity) == LootSettingsEnum.None)
                        PlayerHelper.addDropsToPlayer(player, entityItem.item, false)
                    else if (NMDConfig.lootcfg.lootModule && player.getDistanceSq(event.entityLiving) <= PlayerHelper.squareSum(NMDConfig.lootcfg.distanceForDrop.toDouble())){
                        MinecraftForge.EVENT_BUS.post(LootDropEvent(SimpleEntityItem(entityItem), party, rarity, false))
                    }
                    else
                        MinecraftForge.EVENT_BUS.post(LootDropEvent(SimpleEntityItem(entityItem), party, rarity, true))
                }
                event.drops.clear()
            } else {
                event.drops.removeAll { entityItem ->
                    PlayerHelper.addDropsToPlayer(player, entityItem.item, false)
                }
            }
            if (event.drops.isEmpty())
                event.isCanceled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onLivingExpDrop(event: LivingExperienceDropEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote && cache.containsKey(mob.uniqueID)){
            cache[mob.uniqueID].forEach {player ->
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