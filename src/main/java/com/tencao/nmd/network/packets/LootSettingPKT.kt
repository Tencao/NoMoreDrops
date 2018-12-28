package com.tencao.nmd.network.packets

import be.bluexin.saomclib.packets.AbstractClientPacketHandler
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import com.tencao.nmd.api.*
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.drops.LootRegistry
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IThreadListener
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class LootSettingPKT(): IMessage {

    private var lootSetting : ILootSettings = LootSettingsEnum.Random
    private var rarity : IRarity = DropRarityEnum.UNKNOWN

    constructor(lootSettingsEnum: ILootSettings, dropRarityEnum: IRarity): this(){
        this.lootSetting = lootSettingsEnum
        this.rarity = dropRarityEnum
    }

    override fun fromBytes(buf: ByteBuf) {
        this.lootSetting = LootRegistry.getRegisteredLoot(buf.readString())
        this.rarity = LootRegistry.getRegisteredRarity(buf.readString())
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeString(lootSetting.toString())
        buf.writeString(rarity.toString())
    }

    companion object {
        class Handler : AbstractClientPacketHandler<LootSettingPKT>(){
            override fun handleClientPacket(player: EntityPlayer, message: LootSettingPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    player.getNMDData().setLootSetting( message.lootSetting, message.rarity)
                    player.sendStatusMessage(TextComponentTranslation("nmd.lootsetting.changed"), false)
                }
                return null
            }
        }
    }
}