package com.tencao.nmd.events.listener

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.party.PlayerInfo
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.LootSettingsEnum
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.util.PartyHelper
import com.tencao.nmd.util.PlayerHelper
import com.tencao.nmd.events.handler.LootDropEvent
import com.tencao.nmd.registry.LootTableMapper
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.entities.EntityPartyItem
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent

object EntityItemEventListener {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun entityItemSpawnEvent(e: EntityJoinWorldEvent){
        if (!e.world.isRemote && e.entity is EntityItem && e.entity !is EntityPartyItem) {
            val entityItem = e.entity as EntityItem
            if (entityItem.thrower.isNullOrBlank()){
                if (entityItem.owner.isNullOrEmpty() && entityItem.thrower.isNullOrBlank()) {
                    (BlockEventListener.explosionCache.asIterable().firstOrNull { it.test(e.entity.position) })?.let {
                        if (it.player != null){
                            val rarity = LootTableMapper.getRarity(entityItem.item)
                            val lootSettings = it.player.getPartyCapability().party?.leaderInfo?.player?.getNMDData()?.getLootSetting(rarity)?: it.player.getNMDData().getLootSetting(rarity)
                            MinecraftForge.EVENT_BUS.post(LootDropEvent(SimpleEntityItem(entityItem), it.player, rarity, lootSettings, false))

                        }
                        e.isCanceled = true
                        return
                    }
                    e.world.getClosestPlayerToEntity(entityItem, PlayerHelper.squareSum(NMDConfig.lootcfg.distanceForDrop.toDouble()))?.let { player ->
                        if (!isDeadZone(player.getDistanceSq(entityItem))){
                            val rarity = LootTableMapper.getRarity(entityItem.item)
                            val lootSettings = player.getPartyCapability().party?.leaderInfo?.player?.getNMDData()?.getLootSetting(rarity)?: player.getNMDData().getLootSetting(rarity)
                            MinecraftForge.EVENT_BUS.post(LootDropEvent(SimpleEntityItem(entityItem), player, rarity, lootSettings, false))
                            e.isCanceled = true
                        }

                    }
                }
                else {
                    e.world.playerEntities.find { it.name == entityItem.owner }?.let {player ->
                        val distance: Double = player.getDistanceSq(entityItem)
                        if (!isDeadZone(distance) && distance <= PlayerHelper.squareSum(NMDConfig.lootcfg.distanceForDrop.toDouble())){
                            val rarity = LootTableMapper.getRarity(entityItem.item)
                            LootDropEvent(SimpleEntityItem(entityItem), player, rarity, LootSettingsEnum.None, false)
                            e.isCanceled = true
                        }
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