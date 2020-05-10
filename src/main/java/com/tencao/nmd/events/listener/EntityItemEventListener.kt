package com.tencao.nmd.events.listener

import com.teamwizardry.librarianlib.features.helpers.getNBTBoolean
import com.teamwizardry.librarianlib.features.helpers.getNBTList
import com.teamwizardry.librarianlib.features.helpers.hasNBTEntry
import com.teamwizardry.librarianlib.features.helpers.removeNBTEntry
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.entities.EntityPartyItem
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
        if (!e.world.isRemote && e.entity is EntityItem && e.entity !is EntityPartyItem) {
            val entityItem = e.entity as EntityItem
            val distance = NMDConfig.lootcfg.distanceForDrop
            val players = e.world.getEntitiesWithinAABB(EntityPlayer::class.java, AxisAlignedBB(e.entity.position.add(-distance, -distance, -distance), e.entity.position.add(distance, distance, distance)))
            if (players.isEmpty()) return
            if (entityItem.item.getNBTBoolean(Constants.ignoreData)) return
            if (entityItem.thrower.isNullOrBlank()){
                if (entityItem.owner.isNullOrEmpty() && entityItem.thrower.isNullOrBlank()) {
                    (BlockEventListener.explosionCache.asIterable().firstOrNull { it.test(e.entity.position) })?.let {
                        if (it.player != null){
                            LootHelper.sortLoot(entityItem, it.player.uniqueID)
                        }
                        e.isCanceled = true
                        return
                    }
                    players.minBy { it.getDistance(e.entity) }?.let { player ->
                        if (!isDeadZone(player.getDistanceSq(entityItem))){
                            LootHelper.sortLoot(entityItem, player.uniqueID)
                            e.isCanceled = true
                        }

                    }
                }
                else {
                    players.find { it.name == entityItem.owner }?.let {player ->
                        if (!isDeadZone(player.getDistanceSq(entityItem))){
                            LootHelper.sortLoot(entityItem, player.uniqueID)
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
                val party = mutableSetOf<UUID>()
                for (i in 0 until tagList.count()) {
                    val tag = tagList.getCompoundTagAt(i)
                    party.add(UUID.fromString(tag.getString(Constants.uuid)))
                }
                if (party.any { e.entityPlayer.uniqueID == it }) {
                    e.item.item.removeNBTEntry(Constants.partyData)
                    e.item.item.removeNBTEntry(Constants.ignoreData)
                    LootHelper.handleLoot(SimpleEntityItem(e.item as EntityItem), party.toList())
                    e.item.setDead()
                }
                e.isCanceled = true
            }
        }
        else {
            e.item.item.removeNBTEntry(Constants.partyData)
            e.item.item.removeNBTEntry(Constants.ignoreData)
        }
    }
}