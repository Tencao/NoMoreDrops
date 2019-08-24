package com.tencao.nmd.data

import io.netty.buffer.ByteBuf
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import java.util.function.Predicate

/**
 * Simple stack data sent and stored on the client
 */
data class SimpleStack(var nbt: NBTTagCompound): Predicate<ItemStack> {

    constructor(stack: ItemStack): this(stack.serializeNBT())

    constructor(item: Item, meta: Int): this(ItemStack(item, 1, meta))

    val stack: ItemStack
        get() = ItemStack(nbt)

    val item: Item
        get() = stack.item

    var count: Int
        get() = nbt.getInteger("Count")
        set(value) = nbt.setInteger("Count", value)

    var meta: Int
        get() = nbt.getInteger("Damage")
        set(value) = nbt.setInteger("Damage", value)

    fun isEmpty(): Boolean{
        return count <= 0
    }

    fun reduce(){
        count = count--
    }

    override fun test(t: ItemStack): Boolean {
        return ItemStack.areItemStacksEqual(t, stack)
    }

    fun toBytes(buf: ByteBuf) {
        PacketBuffer(buf).writeCompoundTag(nbt)
    }

    companion object {
        fun fromBytes(buf: ByteBuf): SimpleStack {
            return SimpleStack(PacketBuffer(buf).readCompoundTag()!!)
        }
    }
}
