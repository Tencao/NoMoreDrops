package com.tencao.nmd.events.listener

import be.bluexin.saomclib.party.PlayerInfo
import be.bluexin.saomclib.party.playerInfo
import com.teamwizardry.librarianlib.features.helpers.*
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.registry.LootRegistry
import com.tencao.nmd.util.Constants
import com.tencao.nmd.util.LootHelper
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.EntityItemPickupEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object EntityItemEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun entityItemSpawnEvent(e: EntityJoinWorldEvent){
        if (!e.world.isRemote && e.entity is EntityItem) {
            val entityItem = e.entity as EntityItem
            if (entityItem.item.getNBTBoolean(Constants.ignoreData)) return
            val distance = NMDConfig.lootcfg.distanceForDrop
            val players = e.world.getEntitiesWithinAABB(EntityPlayer::class.java, AxisAlignedBB(e.entity.position.add(-distance, -distance, -distance), e.entity.position.add(distance, distance, distance)))
            if (players.isEmpty()) return
            if (players.any { isDeadZone(it.getDistanceSq(entityItem)) }) return
            players.filter { it.canEntityBeSeen(entityItem) }
            if (entityItem.thrower.isNullOrBlank()){
                if (entityItem.owner.isNullOrEmpty() && entityItem.thrower.isNullOrBlank()) {
                    (BlockEventListener.explosionCache.asIterable().firstOrNull { it.test(e.entity.position) })?.let {
                        if (it.player != null){
                            LootHelper.sortLoot(entityItem, it.player.playerInfo())
                        }
                        e.isCanceled = true
                        return
                    }
                    players.minBy { it.getDistance(e.entity) }?.let { player ->
                        if (!isDeadZone(player.getDistanceSq(entityItem))){
                            LootHelper.sortLoot(entityItem, player.playerInfo())
                            e.isCanceled = true
                        }

                    }
                }
                else {
                    players.find { it.name == entityItem.owner }?.let {player ->
                        if (!isDeadZone(player.getDistanceSq(entityItem))){
                            LootHelper.sortLoot(entityItem, player.playerInfo())
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


    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onEntityPickUp(e: EntityItemPickupEvent) {
        if (e.item.item.hasNBTEntry(Constants.partyData)) {
            e.item.item.getNBTList(Constants.partyData, 10)?.let { tagList ->
                val party = mutableSetOf<PlayerInfo>()
                for (i in 0 until tagList.count()) {
                    val tag = tagList.getCompoundTagAt(i)
                    party.add(PlayerInfo(UUID.fromString(tag.getString(Constants.uuid))))
                }
                if (party.any { it == e.entityPlayer.playerInfo() }) {
                    e.item.item.removeNBTEntry(Constants.partyData)
                    e.item.item.removeNBTEntry(Constants.ignoreData)
                    val rarity = e.item.item.getNBTInt(Constants.rarity)
                    e.item.item.removeNBTEntry(Constants.rarity)
                    val simpleEntityItem = SimpleEntityItem(e.item as EntityItem)
                    simpleEntityItem.rarity = LootRegistry.registeredRarity[rarity]
                    LootHelper.handleLoot(simpleEntityItem, party.toList())
                    e.item.setDead()
                }
                e.isCanceled = true
            }
        }
        else {
            e.item.item.removeNBTEntry(Constants.partyData)
            e.item.item.removeNBTEntry(Constants.ignoreData)
            e.item.item.removeNBTEntry(Constants.rarity)
        }
    }
}