package com.tencao.nmd.events

import be.bluexin.saomclib.capabilities.PartyCapability
import com.tencao.nmd.util.PlayerHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class LivingEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingDrop(event: LivingDropsEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote){
            mob.combatTracker.combatEntries.firstOrNull { PlayerHelper.isPlayer(it.damageSrc.trueSource) }?.let { source ->
                val player = source.damageSrc.trueSource as EntityPlayer
                val party = player.getCapability(PartyCapability.CAP_INSTANCE, null)!!.party
                if (party != null) {
                    event.drops.removeAll{PlayerHelper.addDropsToParty(player, it.item, true) && it.isDead}
                } else
                    event.drops.removeAll{PlayerHelper.addDropsToPlayer(player, it.item, false) && it.isDead}
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingExpDrop(event: LivingExperienceDropEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote){
            val players = mob.combatTracker.combatEntries.filter { PlayerHelper.isPlayer(it.damageSrc.trueSource) }.toList()
            players.forEach {
                PlayerHelper.addExpToParty(it.damageSrc.trueSource as EntityPlayer, event.droppedExperience / players.size)
            }
        }
    }
}