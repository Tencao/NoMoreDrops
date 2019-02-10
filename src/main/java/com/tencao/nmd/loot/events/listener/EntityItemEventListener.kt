package com.tencao.nmd.loot.events.listener

import be.bluexin.saomclib.capabilities.getPartyCapability
import com.tencao.nmd.party.LootSettingsEnum
import com.tencao.nmd.core.capability.getNMDData
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.core.util.PartyHelper
import com.tencao.nmd.core.util.PlayerHelper
import com.tencao.nmd.loot.events.handler.LootDropEvent
import com.tencao.nmd.party.registry.LootTableMapper
import com.tencao.nmd.party.data.SimpleEntityItem
import com.tencao.nmd.party.entities.EntityPartyItem
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EntityItemEventListener {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun entityItemSpawnEvent(e: EntityJoinWorldEvent){
        if (!e.world.isRemote && e.entity is EntityItem && e.entity !is EntityPartyItem) {
            val entityItem = e.entity as EntityItem
            if (entityItem.thrower.isNullOrBlank()){
                if (entityItem.owner.isNullOrEmpty() && entityItem.thrower.isNullOrBlank()) {
                    e.world.getNearestPlayerNotCreative(entityItem, PlayerHelper.squareSum(NMDConfig.lootcfg.distanceForDrop.toDouble()))?.let { player ->
                        if (!isDeadZone(player.getDistanceSq(entityItem))){
                            val party = player.getPartyCapability().getOrCreatePT()
                            if (PartyHelper.isValidParty(party)){
                                val leaderNMDData = party.leader!!.getNMDData()
                                val rarity = LootTableMapper.getRarity(entityItem.item)
                                val lootSetting = leaderNMDData.getLootSetting(rarity)
                                if (lootSetting != LootSettingsEnum.None) {
                                    MinecraftForge.EVENT_BUS.post(LootDropEvent(SimpleEntityItem(entityItem), party, rarity, false))
                                    e.isCanceled = true
                                }
                                else
                                    e.isCanceled = PlayerHelper.addDropsToPlayer(player, entityItem, false)
                            }
                            else {
                                e.isCanceled = PlayerHelper.addDropsToPlayer(player, entityItem, false)
                            }
                        }

                    }
                }
                else {
                    e.world.playerEntities.find { it.name == entityItem.owner }?.let {player ->
                        val distance: Double = player.getDistanceSq(entityItem)
                        if (!isDeadZone(distance) && distance <= PlayerHelper.squareSum(NMDConfig.lootcfg.distanceForDrop.toDouble()))
                            e.isCanceled = PlayerHelper.addDropsToPlayer(player, entityItem, false)
                    }
                }
            }
        }
    }

    /**
     * This is the distance from the player where typically thrown items are spawned
     * It's important that we do not catch drops from this radius, as doing so would
     * prevent players from throwing away items.
     */
    private fun isDeadZone(distance: Double): Boolean{
        return distance in 1.737..1.747
    }
}