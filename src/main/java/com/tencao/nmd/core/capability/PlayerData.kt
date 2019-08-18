package com.tencao.nmd.core.capability

import be.bluexin.saomclib.capabilities.AbstractCapability
import be.bluexin.saomclib.capabilities.AbstractEntityCapability
import be.bluexin.saomclib.capabilities.Key
import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.packets.PacketPipeline
import be.bluexin.saomclib.party.IPlayerInfo
import com.tencao.nmd.core.NMDCore
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.core.data.SimpleStack
import com.tencao.nmd.party.LootSettingsEnum
import com.tencao.nmd.core.gui.LootGUI
import com.tencao.nmd.party.data.ClientLootObject
import com.tencao.nmd.party.network.packets.LootSyncAllPKT
import com.tencao.nmd.party.network.packets.LootSyncPKT
import com.tencao.nmd.party.registry.LootRegistry
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import java.util.*
import kotlin.collections.HashSet

class PlayerData : AbstractEntityCapability() {

    var itemBlacklist = HashSet<SimpleStack>()
    lateinit var player: EntityPlayer
        private set

    val lootSettings: LinkedHashMap<IRarity, ILootSettings> = linkedMapOf()
    get() {
        //Sanity Check
        if (field.isEmpty()){
            //In rare cases this is empty, rebuild the map
            LootRegistry.defaultLootPairings.toMap(field)
        }
        return field
    }

    // Block Loot will always be classed as Unknown rarity
    var extraBlocksAsLoot = false
    var upgradeEnchantRarity = true


    val lootDrops = mutableListOf<ClientLootObject>()

    override fun setup(param: Any): AbstractCapability {
        super.setup(param)
        this.player = param as EntityPlayer
        if (NMDConfig.partycfg.partyModule && !this.player.world.isRemote) {
            LootRegistry.defaultLootPairings.toMap(lootSettings)
        }
        return this
    }

    fun modifyBlackList(itemStack: ItemStack): Boolean{
        return itemBlacklist.removeIf { it.test(itemStack) } or itemBlacklist.add(SimpleStack(itemStack))
    }

    fun isBlackListed(itemStack: ItemStack): Boolean{
        return itemBlacklist.any { it.test(itemStack) }
    }

    fun setItemList(items: HashSet<SimpleStack>){
        itemBlacklist.clear()
        items.forEach { itemBlacklist.add(it) }
    }

    fun getItemList(): HashSet<ItemStack> {
        return itemBlacklist.asSequence().map { it.stack }.toHashSet()
    }

    fun tickLoot(){
        lootDrops.removeAll {
            if (--it.tickTime <= 0){
                LootGUI.recalculateFrom(it.lootSetting)
                return@removeAll true
            }
            false
        }
    }

    /**
     * @param sync If true, it will send a sync packet to the server/client
     */
    fun setLootSetting(rarity: IRarity, lootSetting: ILootSettings, sync: Boolean){
        if (!player.world.isRemote && player.getPartyCapability().getOrCreatePT().isLeader(player)){
            player.getPartyCapability().getOrCreatePT().membersInfo.mapNotNull(IPlayerInfo::player)
                    .asSequence()
                    .filter { it != player }
                    .forEach {partyMember -> partyMember.getNMDData().setLootSetting(rarity, lootSetting, sync)
            }
        }
        lootSettings.replace(rarity, lootSetting)
        if (!player.world.isRemote && sync) {
            PacketPipeline.sendTo(LootSyncPKT(rarity, lootSetting), player as EntityPlayerMP)
        }
    }

    fun setLootSetting(lootSettings: LinkedHashMap<IRarity, ILootSettings>){
        this.lootSettings.clear()
        this.lootSettings.putAll(lootSettings)

        if (!player.world.isRemote) {
            PacketPipeline.sendTo(LootSyncAllPKT(lootSettings), player as EntityPlayerMP)
        }
    }

    fun resetLootSettings(){
        this.lootSettings.clear()
        this.lootSettings.putAll(LootRegistry.defaultLootPairings)
    }

    fun getLootSetting(rarity: IRarity): ILootSettings {
        return lootSettings[rarity]?: LootSettingsEnum.Random
    }

    override val shouldSyncOnDeath = true

    override val shouldSyncOnDimensionChange = true

    override val shouldRestoreOnDeath = true

    override val shouldSendOnLogin = true

    class Storage : Capability.IStorage<PlayerData> {
        override fun writeNBT(capability: Capability<PlayerData>?, instance: PlayerData, side: EnumFacing?): NBTBase? {
            val tag = NBTTagCompound()
            val items = NBTTagList()

            instance.itemBlacklist.forEach{
                items.appendTag(it.nbt)
            }

            tag.setTag("items", items)
            return tag
        }

        override fun readNBT(capability: Capability<PlayerData>?, instance: PlayerData, side: EnumFacing?, nbt: NBTBase?) {
            if (nbt !is NBTTagCompound) return

            val items = nbt.getTagList("items", 10)
            val itemCount = items.tagCount()
            instance.itemBlacklist.clear()

            var tag: NBTTagCompound
            for (i in 0 until itemCount) {
                tag = items.getCompoundTagAt(i)
                instance.itemBlacklist.add(SimpleStack(tag))
            }
        }

    }

    companion object {
        @Key
        var KEY = ResourceLocation(NMDCore.MODID, "player_data")

        @CapabilityInject(PlayerData::class)
        lateinit var CAPABILITY: Capability<PlayerData>
    }
}

fun EntityPlayer.getNMDData() = this.getCapability(PlayerData.CAPABILITY, null)!!