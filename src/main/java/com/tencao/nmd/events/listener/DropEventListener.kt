package com.tencao.nmd.events.listener

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.onServer
import be.bluexin.saomclib.party.PlayerInfo
import be.bluexin.saomclib.party.playerInfo
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.util.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object DropEventListener {

    private val expCache: HashMap<UUID, List<PlayerInfo>> = hashMapOf()

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingDrop(event: LivingDropsEvent) {
        val mob = event.entityLiving?: return

        val players = getPlayers(mob)

        mob.world.onServer {
            when {
                players.count() == 1 ->
                    event.drops.removeIf { entityItem ->
                        if (NMDConfig.lootcfg.alwaysDropMobLoot)
                            SimpleEntityItem(entityItem).spawnEntityPartyItem(players, false)
                        else
                            LootHelper.sortLoot(entityItem, players.first())
                        true
                    }
                players.count() > 1 ->
                    event.drops.removeIf { entityItem ->
                        if (NMDConfig.lootcfg.alwaysDropMobLoot)
                            SimpleEntityItem(entityItem).spawnEntityPartyItem(players, false)
                        else
                            LootHelper.sortLoot(entityItem, players)
                        true
                    }
                else -> {
                    event.drops.removeIf { entityItem ->
                        SimpleEntityItem(entityItem).spawnEntityItem()
                        true
                    }
                }
            }

            event.isCanceled = event.drops.isEmpty()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingExpDrop(event: LivingExperienceDropEvent) {
        val mob = event.entityLiving ?: return
        val players = getExpCache(mob.uniqueID).filter { it.player != null }
        mob.world.onServer {
            players.forEach { playerInfo ->
                val player = playerInfo.player?: return@forEach
                val party = player.getPartyCapability().partyData
                val xp = event.droppedExperience / players.count()
                if (PartyHelper.isValidParty(party)) {
                    PartyHelper.addExpToParty(player, event.droppedExperience / players.count())
                } else {
                    player.addExperience(event.droppedExperience / players.count())
                }
                event.droppedExperience -= xp
            }

            if (event.droppedExperience == 0)
                event.isCanceled = true

        }
    }

    fun getPlayers(mob: EntityLivingBase): List<PlayerInfo>{
        val combatEntries = mob.combatTracker.getCombatEntries().asSequence()
                .filter{ PlayerHelper.isPlayer(it.damageSrc.trueSource)}


        val players: List<PlayerInfo>

        // ****** Handle Loot share ******
        if (NMDConfig.lootcfg.firstHit) {
            val player: EntityPlayer = combatEntries.first().damageSrc.trueSource as EntityPlayer
            players = player.getPartyCapability().partyData?.getMembers()?.toList()?: listOf(player.playerInfo())
        }
        else {
            val set: MutableMap<Sequence<PlayerInfo>, Float> = mutableMapOf()
            combatEntries
                    .forEach { combatEntry ->
                        val player: EntityPlayer = combatEntry.damageSrc.trueSource as EntityPlayer
                        val party = player.getPartyCapability().partyData?.getMembers()?.asSequence()?: sequenceOf(player.playerInfo())
                        set[party] = set[party]?: 0f + combatEntry.damageAmount
                    }
            players = set.maxBy { it.value }?.key?.toList()?: emptyList()
        }

        // ****** Handle Exp share ******
        if (NMDConfig.lootcfg.expForAll) {
            val list = mutableListOf<PlayerInfo>()
            combatEntries.forEach { entry ->
                val player = (entry.damageSrc.trueSource as EntityPlayer)
                list.addAll(player.getPartyCapability().partyData?.getMembers()?: listOf(player.playerInfo()))
            }
            expCache[mob.uniqueID] = list
        }
        else
            expCache[mob.uniqueID] = players

        return players

    }

    fun getExpCache(mob: UUID): List<PlayerInfo>{
        return expCache.remove(mob)?: emptyList()
    }

    fun resetData(mob: EntityLivingBase){
        // ****** Ensure all old combat data is reset here ******
        mob.combatTracker.getCombatEntries().clear()
        mob.combatTracker.setLastCombatTime(mob.combatTracker.fighter.ticksExisted - 500)
        mob.combatTracker.reset()
    }

}