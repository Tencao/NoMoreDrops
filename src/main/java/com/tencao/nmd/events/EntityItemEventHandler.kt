package com.tencao.nmd.events

import com.tencao.nmd.util.PlayerHelper
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class EntityItemEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun entityItemSpawnEvent(e: EntityJoinWorldEvent){
        if (!e.world.isRemote && e.entity is EntityItem) {
            val item = e.entity as EntityItem
            if (item.owner.isNullOrEmpty()) {
                val player = e.world.getNearestPlayerNotCreative(item, 10.0)
                val distance: Float = player?.getDistanceToEntity(item)?: 0f
                if (player != null && !isDeadZone(distance))
                    e.isCanceled = PlayerHelper.addDropsToPlayer(player, item, false)
            }
            else {
                val player = e.world.playerEntities.find { it.name == item.owner }
                val distance: Float = player?.getDistanceToEntity(item)?: 0f
                if (player != null && !isDeadZone(distance) && distance <= 10.0)
                    e.isCanceled = PlayerHelper.addDropsToPlayer(player, item, false)
            }
        }
    }

    /**
     * This is the distance from the player where typically thrown items are spawned
     * It's important that we do not catch drops from this radius, as doing so would
     * prevent players from throwing away items.
     */
    private fun isDeadZone(notFloat: Float): Boolean{
        return notFloat in 1.318..1.322
    }
}