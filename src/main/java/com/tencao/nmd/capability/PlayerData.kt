package com.tencao.nmd.capability

import be.bluexin.saomclib.capabilities.AbstractCapability
import be.bluexin.saomclib.capabilities.AbstractEntityCapability
import be.bluexin.saomclib.capabilities.Key
import com.tencao.nmd.NMDCore
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject

class PlayerData : AbstractEntityCapability() {

    var itemBlacklist: MutableList<ResourceLocation> = ArrayList()
    lateinit var player: EntityPlayer
        private set

    override fun setup(param: Any): AbstractCapability {
        super.setup(param)
        this.player = param as EntityPlayer
        return this
    }

    fun addItemToList(itemStack: ItemStack): Boolean{
        val item: ResourceLocation = Item.REGISTRY.getNameForObject(itemStack.item)!!
        return if (!itemBlacklist.contains(item)) {
            itemBlacklist.add(item)
            true
        }
        else {
            itemBlacklist.remove(item)
            false
        }
    }

    fun isBlackListed(itemStack: ItemStack): Boolean{
        return itemBlacklist.any { it == Item.REGISTRY.getNameForObject(itemStack.item) }
    }

    fun setItemList(items: NonNullList<ItemStack>){
        itemBlacklist.clear()
        items.forEach { itemBlacklist.add(Item.REGISTRY.getNameForObject(it.item)!!) }
    }

    fun getItemList(): NonNullList<ItemStack> {
        val items: NonNullList<ItemStack> = NonNullList.create()
        itemBlacklist.forEach{ it -> items.add(ItemStack(Item.REGISTRY.getObject(it)))}
        return items
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
                itemInfo.setString("name", it.toString())
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
                instance.itemBlacklist.add(ResourceLocation(tag.getString("name")))
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