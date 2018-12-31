package com.tencao.nmd.network.packets

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.packets.AbstractClientPacketHandler
import be.bluexin.saomclib.packets.AbstractServerPacketHandler
import be.bluexin.saomclib.readString
import be.bluexin.saomclib.writeString
import com.tencao.nmd.api.DropRarityEnum
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.api.SpecialLootSettingsEnum
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.data.ClientLootObject
import com.tencao.nmd.data.SimpleStack
import com.tencao.nmd.drops.LootRegistry
import com.tencao.nmd.gui.LootGUI
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

class LootClientPKT(): IMessage {

    private var stack: SimpleStack = SimpleStack(ItemStack.EMPTY)
    private var timer: Int = 0
    private var rollID: UUID = UUID.randomUUID()
    private var rarity: IRarity = DropRarityEnum.UNKNOWN
    private var lootSetting : ISpecialLootSettings = SpecialLootSettingsEnum.NeedBeforeGreed

    constructor(itemStack: SimpleStack, timer: Int, rollID: UUID, rarity: IRarity, lootSetting: ISpecialLootSettings): this(){
        this.stack = itemStack
        this.timer = timer
        this.rollID = rollID
        this.rarity = rarity
        this.lootSetting = lootSetting
    }

    override fun fromBytes(buf: ByteBuf) {
        this.stack = SimpleStack.fromBytes(buf)
        this.timer = buf.readInt()
        this.rollID = UUID.fromString(buf.readString())
        this.rarity = LootRegistry.getRegisteredRarity(buf.readString())
        this.lootSetting = LootRegistry.getRegisteredLoot(buf.readString()) as ISpecialLootSettings
    }

    override fun toBytes(buf: ByteBuf) {
        stack.toBytes(buf)
        buf.writeInt(timer)
        buf.writeString(rollID.toString())
        buf.writeString(rarity.toString())
        buf.writeString(lootSetting.toString())
    }

    companion object {
        class Handler : AbstractClientPacketHandler<LootClientPKT>(){
            override fun handleClientPacket(player: EntityPlayer, message: LootClientPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    val nmdData = player.getNMDData()
                    val clientLootObject = ClientLootObject(message.stack, message.rollID, message.timer, message.rarity, message.lootSetting, message.lootSetting.createClientCache(message.stack.toStack(), player.getPartyCapability().party!!))
                    nmdData.lootDrops.add(clientLootObject)
                    LootGUI.calculateXY(clientLootObject)
                }
                return null
            }
        }
    }

}

class LootServerPKT(): IMessage {

    private var rollID: UUID = UUID.randomUUID()
    private var lootSetting : ISpecialLootSettings = SpecialLootSettingsEnum.NeedBeforeGreed
    private var cache: Any? = null

    constructor(rollID: UUID, lootSetting: ISpecialLootSettings, cache: Any?): this(){
        this.rollID = rollID
        this.lootSetting = lootSetting
        this.cache = cache
    }

    override fun fromBytes(buf: ByteBuf) {
        this.rollID = UUID.fromString(buf.readString())
        this.lootSetting = LootRegistry.getRegisteredLoot(buf.readString()) as ISpecialLootSettings
        this.cache = lootSetting.fromBytes(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeString(rollID.toString())
        buf.writeString(lootSetting.toString())
        this.lootSetting.toBytes(buf, cache)
    }

    companion object {
        class Handler : AbstractServerPacketHandler<LootServerPKT>(){
            override fun handleServerPacket(player: EntityPlayer, message: LootServerPKT, ctx: MessageContext, mainThread: IThreadListener): IMessage? {
                mainThread.addScheduledTask {
                    LootRegistry.lootdrops.firstOrNull { it.rollID == message.rollID }?.processClientData(player, message.cache)
                }
                return null
            }
        }
    }

}