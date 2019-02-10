package com.tencao.nmd.loot.events.listener

import com.tencao.nmd.core.events.listener.LivingDeathListener.cache
import com.tencao.nmd.core.util.PlayerHelper
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DropEventListener {


    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingDrop(event: LivingDropsEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote && cache.containsKey(mob.uniqueID)){
            event.drops.removeAll { entityItem ->
                PlayerHelper.addDropsToPlayer(cache[mob.uniqueID].first(), entityItem.item, false)
            }
            if (event.drops.isEmpty())
                event.isCanceled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingExpDrop(event: LivingExperienceDropEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote && cache.containsKey(mob.uniqueID)){
            cache[mob.uniqueID].forEach {player ->
                player.addExperience(event.droppedExperience / cache[mob.uniqueID].size)
            }
            event.droppedExperience = 0
            event.isCanceled = true
        }
    }
}