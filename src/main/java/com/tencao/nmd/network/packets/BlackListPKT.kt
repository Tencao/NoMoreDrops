package com.tencao.nmd.network.packets

import be.bluexin.saomclib.packets.AbstractPacketHandler
import com.tencao.nmd.capability.getNMDData
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IThreadListener
import net.minecraft.util.NonNullList
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class BlackListPKT() : IMessage {

    val items: NonNullList<ItemStack> = NonNullList.create()

    constructor(items: NonNullList<ItemStack>): this(){
        this.items.addAll(items)
    }

    private var count = 0

    override fun fromBytes(buf: ByteBuf?) {
        count = buf!!.readInt()
        for (i in 0 until count) {
            items.add(ByteBufUtils.readItemStack(buf))
        }
    }

    override fun toBytes(buf: ByteBuf?) {
        buf!!.writeInt(count)
        items.forEach { ByteBufUtils.writeItemStack(buf, it) }
    }

    companion object {
        class Handler : AbstractPacketHandler<BlackListPKT>() {
            override fun handleClientPacket(player: EntityPlayer, message: BlackListPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                Minecraft.getMinecraft().addScheduledTask { Minecraft.getMinecraft().player.getNMDData().setItemList(message.items)}
                return null
            }

            override fun handleServerPacket(player: EntityPlayer, message: BlackListPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                ctx.serverHandler.player.server.addScheduledTask { ctx.serverHandler.player.getNMDData().setItemList(message.items)}
                return null
            }
        }
    }

    init {
        this.count = items.size
    }
}