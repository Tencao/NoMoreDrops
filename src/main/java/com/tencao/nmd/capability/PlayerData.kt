package com.tencao.nmd.capability

import be.bluexin.saomclib.capabilities.AbstractCapability
import be.bluexin.saomclib.capabilities.AbstractEntityCapability
import be.bluexin.saomclib.capabilities.Key
import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.NMDCore
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.api.LootSettingsEnum
import com.tencao.nmd.data.ClientLootObject
import com.tencao.nmd.data.SimpleStack
import com.tencao.nmd.drops.LootRegistry
import com.tencao.nmd.gui.ItemRollGUI
import com.tencao.nmd.network.packets.LootSettingPKT
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.fml.common.FMLCommonHandler
import java.util.*
import kotlin.collections.HashSet

class PlayerData : AbstractEntityCapability() {

    var itemBlacklist = HashSet<SimpleStack>()
    lateinit var player: EntityPlayer
        private set

    val lootTypes: LinkedHashSet<ILootSettings> = LinkedHashSet()
    val lootSettings: LinkedHashSet<Pair<ILootSettings, IRarity>> = LinkedHashSet()

    var extraBlocksAsLoot = false
    var upgradeEnchantRarity = true


    val lootDrops = mutableListOf<ClientLootObject>()

    /**
     * The last party member number used for the round robin list
     */
    var lastMember: Short = 0

    override fun setup(param: Any): AbstractCapability {
        super.setup(param)
        this.player = param as EntityPlayer
        lootSettings.addAll(LootRegistry.defaultLootPairings)
        return this
    }

    fun modifyBlackList(itemStack: ItemStack): Boolean{
        return itemBlacklist.removeIf { it.test(itemStack) } or itemBlacklist.add(SimpleStack(itemStack))
    }

    fun isBlackListed(itemStack: ItemStack): Boolean{
        return itemBlacklist.any { it.test(itemStack) }
    }

    fun setItemList(items: NonNullList<ItemStack>){
        itemBlacklist.clear()
        items.forEach { itemBlacklist.add(SimpleStack(it)) }
    }

    fun getItemList(): NonNullList<ItemStack> {
        val items: NonNullList<ItemStack> = NonNullList.create()
        itemBlacklist.forEach{ it -> items.add(it.toStack())}
        return items
    }

    fun tickLoot(){
        lootDrops.removeAll {
            if (--it.tickTime <= 0){
                ItemRollGUI.recalculateFrom(it.lootSetting)
                return@removeAll true
            }
            false
        }
    }

    fun setLootSetting(lootSettingsEnum: ILootSettings, dropRarityEnum: IRarity){
        lootSettings.removeIf{ it.second == dropRarityEnum}
        lootSettings.add(Pair(lootSettingsEnum, dropRarityEnum))
        if (!player.world.isRemote)
            PacketPipeline.sendTo(LootSettingPKT(lootSettingsEnum, dropRarityEnum), player as EntityPlayerMP)
    }

    fun setLootSetting(lootSettings: LinkedHashSet<Pair<ILootSettings, IRarity>>){
        this.lootSettings.clear()
        this.lootSettings.addAll(lootSettings)
    }

    fun resetLootSettings(){
        this.lootSettings.clear()
        this.lootSettings.addAll(LootRegistry.defaultLootPairings)
    }

    fun getLootSetting(rarity: IRarity): ILootSettings {
        return lootSettings.firstOrNull { it.second == rarity }?.first ?: LootSettingsEnum.Random
    }

    override val shouldSyncOnDeath = true

    override val shouldSyncOnDimensionChange = true

    override val shouldRestoreOnDeath = true

    override val shouldSendOnLogin = true

    class Storage : Capability.IStorage<PlayerData> {
        override fun writeNBT(capability: Capability<PlayerData>?, instance: PlayerData, side: EnumFacing?): NBTBase? {
            val tag = NBTTagCompound()
            val items = NBTTagList()

            instance.itemBlacklist.forEach{it ->
                val itemInfo = NBTTagCompound()
                itemInfo.setString("name", it.resource.toString())
                itemInfo.setInteger("count", it.count)
                itemInfo.setInteger("id", it.id)
                itemInfo.setTag("nbt", it.nbt)
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
                instance.itemBlacklist.add(SimpleStack(ResourceLocation(tag.getString("name")), tag.getInteger("count"), tag.getInteger("id"), tag.getTag("nbt") as NBTTagCompound))
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