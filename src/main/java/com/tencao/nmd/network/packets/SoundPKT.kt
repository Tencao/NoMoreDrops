package com.tencao.nmd.network.packets

import be.bluexin.saomclib.packets.AbstractClientPacketHandler
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class SoundPKT() : IMessage {
    lateinit var sound: SoundEvent
    var volume: Float = 0f
    var pitch: Float = 0f

    constructor(sound: SoundEvent, volume: Float, pitch: Float): this(){
        this.sound = sound
        this.volume = volume
        this.pitch = pitch
    }

    override fun fromBytes(buf: ByteBuf) {
        sound = SoundEvent(ResourceLocation(buf.readString()))
        volume = buf.readFloat()
        pitch = buf.readFloat()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeString(sound.soundName.toString())
        buf.writeFloat(volume)
        buf.writeFloat(pitch)
    }

    companion object {
        class Handler : AbstractClientPacketHandler<SoundPKT>() {
            override fun handleClientPacket(player: EntityPlayer, message: SoundPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                Minecraft.getMinecraft().addScheduledTask {
                    Minecraft.getMinecraft().player.playSound(message.sound, message.volume, message.pitch)
                }
                return null
            }
        }
    }
}