package com.tencao.nmd.party.network.packets

import be.bluexin.saomclib.packets.AbstractClientPacketHandler
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.core.capability.getNMDData
import com.tencao.nmd.party.registry.LootRegistry
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

class LootSyncAllPKT(): IMessage {

    val lootSettings: LinkedHashMap<IRarity, ILootSettings> = linkedMapOf()

    constructor(lootSettings: LinkedHashMap<IRarity, ILootSettings>) : this() {
        this.lootSettings.putAll(lootSettings)
    }

    override fun fromBytes(buf: ByteBuf) {
        for (I in 0 until buf.readByte().toInt())
            lootSettings[LootRegistry.getRegisteredRarity(buf.readString())] = LootRegistry.getRegisteredLoot(buf.readString())
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeByte(lootSettings.size)
        lootSettings.asSequence().forEachIndexed { index, entry ->
            buf.writeString(entry.key.toString())
            buf.writeString(entry.value.toString())
        }
    }

    companion object {
        class Handler : AbstractClientPacketHandler<LootSyncAllPKT>() {
            override fun handleClientPacket(player: EntityPlayer, message: LootSyncAllPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    player.getNMDData().setLootSetting(message.lootSettings)
                }
                return null
            }
        }
    }
}
