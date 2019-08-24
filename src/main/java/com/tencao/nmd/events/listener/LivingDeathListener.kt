package com.tencao.nmd.events.listener

import com.google.common.collect.LinkedListMultimap
import com.tencao.nmd.util.EntityLivingReflect
import com.tencao.nmd.util.PlayerHelper
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.Optional
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object LivingDeathListener {

    val cache: LinkedListMultimap<UUID, EntityPlayer> = LinkedListMultimap.create()

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    fun onLivingDeath(event: LivingDeathEvent) {
        val mob = event.entityLiving ?: return
        if (!mob.world.isRemote)
            addDropCache(mob)
    }

    @Optional.Method(modid = "customnpcs")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onNPCDeath(event: noppes.npcs.api.event.NpcEvent.DiedEvent){
        val mob = event.npc as EntityLiving
        if (!mob.world.isRemote)
            addDropCache(mob)
    }

    private fun addDropCache(mob: EntityLivingBase){
        EntityLivingReflect.getCombatEntries(mob).asSequence()
                .filter{ PlayerHelper.isPlayer(it.damageSrc.trueSource)}
                .map { it.damageSrc.trueSource as EntityPlayer }.forEachIndexed { index, player -> cache.put(mob.uniqueID, player) }
    }
}