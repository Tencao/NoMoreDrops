package com.tencao.nmd.events

import com.tencao.nmd.util.PlayerHelper
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class EntityItemEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun entityItemSpawnEvent(e: EntityJoinWorldEvent){
        if (!e.world.isRemote && e.entity is EntityItem) {
            val item = e.entity as EntityItem
            if (item.owner.isNullOrEmpty()) {
                e.world.getNearestPlayerNotCreative(item, PlayerHelper.squareSum(8.0))?.let {
                    if (!isDeadZone(it.getDistanceSq(item)))
                        e.isCanceled = PlayerHelper.addDropsToPlayer(it, item, false)
                }
            }
            else {
                e.world.playerEntities.find { it.name == item.owner }?.let {
                    val distance: Double = it.getDistanceSq(item)
                    if (!isDeadZone(distance) && distance <= PlayerHelper.squareSum(8.0))
                        e.isCanceled = PlayerHelper.addDropsToPlayer(it, item, false)
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