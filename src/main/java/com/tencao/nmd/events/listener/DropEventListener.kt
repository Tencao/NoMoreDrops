package com.tencao.nmd.events.listener

import be.bluexin.saomclib.capabilities.PartyCapability
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.onServer
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.util.LootHelper
import com.tencao.nmd.util.PartyHelper
import com.tencao.nmd.util.PlayerHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object DropEventListener {

    private val expCache: HashMap<UUID, List<UUID>> = hashMapOf()

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
        val players = getExpCache(mob.uniqueID)
        mob.world.onServer {
            players.forEach { UUID ->
                val player = FMLCommonHandler.instance().minecraftServerInstance.playerList.getPlayerByUUID(UUID)
                val party = player.getPartyCapability().partyData
                if (PartyHelper.isValidParty(party)) {
                    PartyHelper.addExpToParty(player, event.droppedExperience / players.count())
                } else {
                    player.addExperience(event.droppedExperience / players.count())
                }
            }

            event.droppedExperience = 0
            event.isCanceled = true

        }
    }

    fun getPlayers(mob: EntityLivingBase): List<UUID>{
        val combatEntries = mob.combatTracker.combatEntries.asSequence()
                .filter{ PlayerHelper.isPlayer(it.damageSrc.trueSource)}


        val players: List<UUID>

        // ****** Handle Loot share ******
        if (NMDConfig.lootcfg.firstHit) {
            val player: EntityPlayer = combatEntries.first().damageSrc.trueSource as EntityPlayer
            players = player.getPartyCapability().partyData?.membersInfo?.map { it.uuid }?: listOf(player.uniqueID)
        }
        else {
            val set: MutableMap<Sequence<UUID>, Float> = mutableMapOf()
            combatEntries
                    .forEach { combatEntry ->
                        val player: EntityPlayer = combatEntry.damageSrc.trueSource as EntityPlayer
                        val players = player.getPartyCapability().partyData?.membersInfo?.map { it.uuid }?.asSequence()?: sequenceOf(player.uniqueID)
                        set[players] = set[players]?: 0f + combatEntry.damageAmount
                    }
            players = set.maxBy { it.value }?.key?.toList()?: emptyList()
        }

        // ****** Handle Exp share ******
        if (NMDConfig.lootcfg.expForAll) {
            val list = mutableListOf<UUID>()
            combatEntries.forEach { entry ->
                val player = (entry.damageSrc.trueSource as EntityPlayer)
                list.addAll(player.getPartyCapability().partyData?.membersInfo?.map { it.uuid }?: listOf(player.uniqueID))
            }
            expCache[mob.uniqueID] = list
        }
        else
            expCache[mob.uniqueID] = players

        return players

    }

    fun getExpCache(mob: UUID): List<UUID>{
        return expCache.remove(mob)?: emptyList()
    }

    fun resetData(mob: EntityLivingBase){
        // ****** Ensure all old combat data is reset here ******
        mob.combatTracker.combatEntries.clear()
        mob.combatTracker.lastDamageTime = mob.combatTracker.fighter.ticksExisted - 500
        mob.combatTracker.reset()
    }

}