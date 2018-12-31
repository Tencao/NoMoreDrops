package com.tencao.nmd.network.packets

import be.bluexin.saomclib.packets.AbstractPacketHandler
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.data.SimpleStack
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class BlackListPKT() : IMessage {

    private var count = 0
    val items: HashSet<SimpleStack> = hashSetOf()

    constructor(items: HashSet<SimpleStack>): this(){
        this.items.addAll(items)
    }


    override fun fromBytes(buf: ByteBuf?) {
        count = buf!!.readInt()
        for (i in 0 until count) {
            items.add(SimpleStack.fromBytes(buf))
        }
    }

    override fun toBytes(buf: ByteBuf?) {
        buf!!.writeInt(count)
        items.forEach { it.toBytes(buf) }
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