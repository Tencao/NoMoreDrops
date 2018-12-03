package com.tencao.nmd.events

import be.bluexin.saomclib.capabilities.PartyCapability
import com.tencao.nmd.NMDCore
import com.tencao.nmd.util.PlayerHelper
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.event.entity.living.*
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class LivingEventHandler {

    /**
     * This is the checker that will check if the mob is the correct
     * level and if not, correct it's level to match nearby players
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun livingEvent(event: LivingEvent) {
        if (event.entityLiving != null) {
            if (!event.entityLiving.world.isRemote && event.entityLiving.isEntityAlive) {
                if (event.entityLiving != null) {
                    val tags = event.entityLiving.entityData

                    if (tags.hasKey(NMDCore.MODID + ":combatTimer")) {
                        var timer = tags.getInteger(NMDCore.MODID + ":combatTimer")
                        if (timer-- <= 0) {
                            tags.removeTag(NMDCore.MODID + ":combatTimer")
                        } else
                            tags.setInteger(NMDCore.MODID + ":combatTimer", timer)
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingDrop(event: LivingDropsEvent) {
        val mob = event.entityLiving
        if (mob != null && !mob.world.isRemote) {
            val player = mob.world.getPlayerEntityByName(mob.entityData.getString(NMDCore.MODID + ":combatTag"))
            if (player != null && PlayerHelper.isPlayer(player)) {
                val party = player.getCapability(PartyCapability.CAP_INSTANCE, null)!!.party
                if (party != null) {
                    event.drops.removeAll{PlayerHelper.addDropsToParty(player, it.entityItem, true) && it.isDead}
                } else
                    event.drops.removeAll{PlayerHelper.addDropsToPlayer(player, it.entityItem, false) && it.isDead}
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun livingDeathEvent(event: LivingDeathEvent) {
        if (event.entityLiving != null) {
            if (!event.entityLiving.world.isRemote && PlayerHelper.isPlayer(event.source.entity)) {
                val target = event.entityLiving
                val killer = event.source.entity as EntityPlayerMP
                //Sanity check
                if (!PlayerHelper.isPlayer(target)) {
                    if (!target.entityData.hasKey(NMDCore.MODID + ":combatTag"))
                        target.entityData.setString(NMDCore.MODID + ":combatTag", killer.name)
                }

            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingExpDrop(event: LivingExperienceDropEvent) {
        val mob = event.entityLiving
        if (mob != null && !mob.world.isRemote) {
            val player = mob.world.getPlayerEntityByName(mob.entityData.getString(NMDCore.MODID + ":combatTag"))
            if (player != null && PlayerHelper.isPlayer(player)) {
                val party = player.getCapability(PartyCapability.CAP_INSTANCE, null)!!.party
                if (party != null) {
                    val partyMembers = party.members.filter { pl -> player.getDistanceToEntity(pl) <= 128}
                    partyMembers.forEach { pl -> pl.addExperience(event.droppedExperience / party.members.count()) }
                } else
                    player.addExperience(event.droppedExperience)
                event.droppedExperience = 0
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingHurt(event: LivingHurtEvent) {
        if (event.entityLiving != null) {
            if (!event.entityLiving.world.isRemote && event.entityLiving.isEntityAlive) {
                val hurt = event.entityLiving
                val source = event.source.entity
                //Sanity check
                if (source != null) {
                    if (PlayerHelper.isPlayer(source) && hurt is EntityCreature && !PlayerHelper.isPlayer(hurt)) {
                        val target = source as EntityPlayerMP

                        if (!hurt.getEntityData().hasKey(NMDCore.MODID + ":combatTag"))
                            hurt.getEntityData().setString(NMDCore.MODID + ":combatTag", target.name)
                    }
                    hurt.entityData.setInteger(NMDCore.MODID + ":combatTimer", 400)
                    source.entityData.setInteger(NMDCore.MODID + ":combatTimer", 400)
                }

            }
        }
    }
}