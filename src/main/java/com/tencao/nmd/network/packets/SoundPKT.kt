package com.tencao.nmd.network.packets

import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class SoundPKT(var sound: SoundEvent, var volume: Float, var pitch: Float) : IMessage {

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

    class Handler : IMessageHandler<SoundPKT, IMessage> {
        override fun onMessage(message: SoundPKT, ctx: MessageContext): IMessage? {
            Minecraft.getMinecraft().addScheduledTask {
                Minecraft.getMinecraft().player.playSound(message.sound, message.volume, message.pitch)
            }
            return null
        }
    }
}