package com.tencao.nmd.core.network.packets

import be.bluexin.saomclib.packets.AbstractServerPacketHandler
import com.tencao.nmd.core.util.Keybinds
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class KeyPressPKT(): IMessage {

    private var key: Keybinds? = null

    constructor(key: Keybinds): this(){
        this.key = key
    }

    override fun fromBytes(buf: ByteBuf?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toBytes(buf: ByteBuf?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        class Handler : AbstractServerPacketHandler<KeyPressPKT>(){
            override fun handleServerPacket(player: EntityPlayer, message: KeyPressPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    //TODO Implement
                }
                return null
            }
        }
    }
}