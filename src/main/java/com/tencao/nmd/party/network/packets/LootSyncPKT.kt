package com.tencao.nmd.party.network.packets

import be.bluexin.saomclib.packets.AbstractPacketHandler
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import com.tencao.nmd.party.DropRarityEnum
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.party.LootSettingsEnum
import com.tencao.nmd.core.capability.getNMDData
import com.tencao.nmd.party.registry.LootRegistry
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class LootSyncPKT(): IMessage {

    private var rarity : IRarity = DropRarityEnum.UNKNOWN
    private var lootSetting : ILootSettings = LootSettingsEnum.Random

    constructor(rarity: IRarity, lootSettings: ILootSettings): this(){
        this.rarity = rarity
        this.lootSetting = lootSettings
    }

    override fun fromBytes(buf: ByteBuf) {
        this.rarity = LootRegistry.getRegisteredRarity(buf.readString())
        this.lootSetting = LootRegistry.getRegisteredLoot(buf.readString())
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeString(rarity.toString())
        buf.writeString(lootSetting.toString())
    }

    companion object {
        class Handler : AbstractPacketHandler<LootSyncPKT>(){
            override fun handleClientPacket(player: EntityPlayer, message: LootSyncPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    player.getNMDData().setLootSetting( message.rarity, message.lootSetting,  false)
                    player.sendStatusMessage(TextComponentTranslation("nmd.lootsetting.changed"), false)
                }
                return null
            }

            override fun handleServerPacket(player: EntityPlayer, message: LootSyncPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    player.getNMDData().setLootSetting(message.rarity, message.lootSetting, true)
                }
                return null
            }
        }
    }
}