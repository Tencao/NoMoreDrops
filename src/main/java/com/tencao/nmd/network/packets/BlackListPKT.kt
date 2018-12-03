package com.tencao.nmd.network.packets

import com.tencao.nmd.capability.getNMDData
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class BlackListPKT(var items: NonNullList<ItemStack>) : IMessage {

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

    class Handler : IMessageHandler<BlackListPKT, IMessage> {
        override fun onMessage(message: BlackListPKT, ctx: MessageContext): IMessage? {

            if (ctx.side.isClient)
                Minecraft.getMinecraft().addScheduledTask { Minecraft.getMinecraft().player.getNMDData().setItemList(message.items)}
            else
                ctx.serverHandler.player.server.addScheduledTask { ctx.serverHandler.player.getNMDData().setItemList(message.items)}
            return null
        }
    }

    init {
        this.count = items.size
    }
}