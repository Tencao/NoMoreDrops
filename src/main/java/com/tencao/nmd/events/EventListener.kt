package com.tencao.nmd.events

import be.bluexin.saomclib.capabilities.PartyCapability
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.events.PartyEvent
import com.tencao.nmd.api.DropRarityEnum
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.api.LootSettingsEnum
import com.tencao.nmd.api.SpecialLootSettingsEnum
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.data.MobCache
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.drops.LootRegistry
import com.tencao.nmd.drops.LootTableMapper
import com.tencao.nmd.entities.EntityPartyItem
import com.tencao.nmd.gui.LootGUI
import com.tencao.nmd.util.*
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.Optional
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.*
import kotlin.collections.HashSet

object BlockEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    fun onBlockDrops(e: BlockEvent.HarvestDropsEvent) {
        if (!e.world.isRemote && PlayerHelper.isPlayer(e.harvester)) {
            e.drops.removeAll{ PlayerHelper.addDropsToPlayer(e.harvester, it, false) && it.isEmpty}
            val party = e.harvester.getPartyCapability().getOrCreatePT()
            if (PartyHelper.isValidParty(party)){
                e.drops.forEach { stack ->
                    PartyLootEvent(SimpleEntityItem(stack, e.pos, e.world), party, party.leader!!.getNMDData().getLootSetting(DropRarityEnum.UNKNOWN), DropRarityEnum.UNKNOWN, UUID.randomUUID())
                }
                e.drops.clear()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockBreak(e: BlockEvent.BreakEvent){
        if (!e.world.isRemote) {
            val tile = e.world.getTileEntity(e.pos)
            val player = e.player
            if (PlayerHelper.isPlayer(player)) {
                if (tile is IInventory) {
                    val inventory = tile as IInventory
                    for (i in 0 until inventory.sizeInventory) {
                        val itemStack = inventory.getStackInSlot(i)
                        if (!itemStack.isEmpty)
                            if (PlayerHelper.addDropsToPlayer(player, itemStack, true) && itemStack.isEmpty)
                                inventory.setInventorySlotContents(i, ItemStack.EMPTY)
                    }
                }
                player.addExperience(e.expToDrop)
                e.expToDrop = 0
            }

        }
    }

}

object EntityItemEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun entityItemSpawnEvent(e: EntityJoinWorldEvent){
        if (!e.world.isRemote && e.entity is EntityItem && e.entity !is EntityPartyItem) {
            val entityItem = e.entity as EntityItem
            if (entityItem.thrower.isNullOrBlank()){
                if (entityItem.owner.isNullOrEmpty() && entityItem.thrower.isNullOrBlank()) {
                    e.world.getNearestPlayerNotCreative(entityItem, PlayerHelper.squareSum(4.0))?.let {player ->
                        if (!isDeadZone(player.getDistanceSq(entityItem))){
                            val party = player.getPartyCapability().getOrCreatePT()
                            if (PartyHelper.isValidParty(party)){
                                val rarity = LootTableMapper.getRarity(entityItem.item)
                                val leaderNMDData = party.leader!!.getNMDData()
                                MinecraftForge.EVENT_BUS.post(PartyLootEvent(SimpleEntityItem(entityItem), party, leaderNMDData.getLootSetting(rarity), rarity, UUID.randomUUID()))
                                e.isCanceled = true
                            }
                            else {
                                e.isCanceled = PlayerHelper.addDropsToPlayer(player, entityItem, false)
                            }
                        }

                    }
                }
                else {
                    e.world.playerEntities.find { it.name == entityItem.owner }?.let {player ->
                        val distance: Double = player.getDistanceSq(entityItem)
                        if (!isDeadZone(distance) && distance <= PlayerHelper.squareSum(8.0))
                            e.isCanceled = PlayerHelper.addDropsToPlayer(player, entityItem, false)
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
}

object LivingEventListener {

    private val dropCache: MutableSet<Pair<UUID, Set<EntityPlayer>>> = mutableSetOf()

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingDeath(event: LivingDeathEvent) {
        val mob = event.entityLiving ?: return
        if (!mob.world.isRemote) {
            dropCache.add(Pair(mob.uniqueID, mob.combatTracker.combatEntries.asSequence()
                    .filter{it -> PlayerHelper.isPlayer(it.damageSrc.trueSource)}
                    .map { it -> it.damageSrc.trueSource as EntityPlayer }.toSet()))
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingDrop(event: LivingDropsEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote){
            val player = dropCache.firstOrNull { it.first == mob.uniqueID }?.second?.firstOrNull()?: return
            val party = player.getPartyCapability().getOrCreatePT()
            if (PartyHelper.isValidParty(party)) {
                val leaderNMDData = party.leader!!.getNMDData()

                event.drops.forEach { entityItem ->
                    val rarity = LootTableMapper.getRarity(entityItem.item)
                    MinecraftForge.EVENT_BUS.post(PartyLootEvent(SimpleEntityItem(entityItem), party, leaderNMDData.getLootSetting(rarity), rarity, UUID.randomUUID()))
                }
                event.drops.clear()
            } else {
                event.drops.removeAll { entityItem ->
                    PlayerHelper.addDropsToPlayer(player, entityItem.item, false)
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLivingExpDrop(event: LivingExperienceDropEvent) {
        val mob = event.entityLiving?: return
        if (!mob.world.isRemote){
            val players = dropCache.firstOrNull { it.first == mob.uniqueID }?.second?: return
            players.forEach {player ->
                val party = player.getCapability(PartyCapability.CAP_INSTANCE, null)!!.getOrCreatePT()
                if (PartyHelper.isValidParty(party)) {
                    PartyHelper.addExpToParty(player, event.droppedExperience / players.size)
                } else {
                    player.addExperience(event.droppedExperience)
                }
            }
            event.droppedExperience = 0
        }
    }

    @Optional.Method(modid = "customnpcs")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onNPCDeath(event: noppes.npcs.api.event.NpcEvent.DiedEvent){
        val mob = event.npc as EntityLiving
        if (!mob.world.isRemote)
            dropCache.add(Pair(mob.uniqueID, mob.combatTracker.combatEntries.asSequence()
                    .filter{it -> PlayerHelper.isPlayer(it.damageSrc.trueSource)}
                    .map { it -> it.damageSrc.trueSource as EntityPlayer }.toSet()))
    }

}

object WorldEventListener {


    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldTick(event: TickEvent.WorldTickEvent){
        if (event.phase == TickEvent.Phase.END)
            if (!event.world.isRemote) {
                if (event.world.provider.dimension == 0) {
                    LootRegistry.lootdrops.asSequence().filter { ++it.tickTime <= event.world.totalWorldTime }.forEach { it.handleLoot() }
                }
            }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldLoad(event: WorldEvent.Load){
        if (event.world.provider.dimension == 0)
            LootTableMapper.generateCache(event.world)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun registerLootRarity(event: RegisterLootRarity){
        event.registerLootRarity(DropRarityEnum.UNKNOWN, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.COMMON, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.UNCOMMON, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.RARE, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.EPIC, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootSettings(LootSettingsEnum.Random)
        event.registerLootSettings(LootSettingsEnum.RoundRobin)

        LootRegistry.registerDefaults(event.getLootSettings(), event.getLootOptions(), event.getRarities())
    }
}

object PlayerEventListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPlayerTick(event: TickEvent.PlayerTickEvent){
        if (event.phase == TickEvent.Phase.END) {
            event.player.getNMDData().tickLoot()
        }
    }

}

object PartyEventListener{

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyJoin(event: PartyEvent.Join){
        event.player.getNMDData().setLootSetting(event.party!!.leader!!.getNMDData().lootSettings)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyLeave(event: PartyEvent.Leave){
        event.player.getNMDData().resetLootSettings()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyDisband(event: PartyEvent.Disbanded){
        event.party?.members?.forEach { it.getNMDData().resetLootSettings() }
        LootRegistry.removeServerLootCache(event.party!!)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPartyLoot(event: PartyLootEvent){
        if (event.lootSetting is ISpecialLootSettings)
            PartyHelper.sendLootPacket(event.entityItem, event.party, event.dropRarity, event.lootSetting as ISpecialLootSettings, event.rollID)
        else {
            event.lootSetting.handleLoot(event.entityItem, event.party, LootRegistry.getServerLootCache(event.lootSetting, event.party))?.let {cache ->
                LootRegistry.updateServerCache(event.lootSetting, event.party, cache)
            }
        }
    }
}

@SideOnly(Side.CLIENT)
object KeyPressListener{

    @SubscribeEvent
    fun keyPress(event: TickEvent.ClientTickEvent){
        ClientKeyHelper.mcToNmd.asSequence().firstOrNull { it.key.isKeyDown }?.let {
            if (it.key.keyDescription == Keybinds.LOOT_GUI.keyName)
                Minecraft.getMinecraft().displayGuiScreen(LootGUI)
        }
    }
}
