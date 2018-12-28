package com.tencao.nmd.api

import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.NMDCore
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.data.ClientLootObject
import com.tencao.nmd.data.RollData
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.gui.ItemRollGUI
import com.tencao.nmd.util.FakeWorld
import com.tencao.nmd.util.PlayerHelper
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import java.awt.Color

enum class LootSettingsEnum: ILootSettings {
    Random{
        override fun handleLoot(entityItem: SimpleEntityItem, party: IParty, cache: Any?) {
            val partyMembers = party.members.filter { pl -> !pl.getNMDData().isBlackListed(entityItem.toStack()) && entityItem.getDistanceSq(pl) <= PlayerHelper.squareSum(64) }.toMutableList()
            if (partyMembers.isNotEmpty()) {
                val stack = entityItem.toStack()
                stack.count = 1
                while (partyMembers.isNotEmpty() && !entityItem.isEmpty()){
                    val member = partyMembers[FakeWorld.fakeWorld.rand.nextInt(partyMembers.size - 1)]
                    if (PlayerHelper.addDropsToPlayer(member, stack, true)){
                        entityItem.shrink()
                    }
                    else partyMembers.remove(member)
                }
            }
            if (!entityItem.isEmpty())
                entityItem.spawnEntityPartyItem(party)
        }
    },
    RoundRobin{
        override fun handleLoot(entityItem: SimpleEntityItem, party: IParty, cache: Any?) {
            val partyMembers = party.members.filter { pl -> !pl.getNMDData().isBlackListed(entityItem.toStack()) && entityItem.getDistanceSq(pl) <= PlayerHelper.squareSum(64) }.toMutableList()
            if (partyMembers.isNotEmpty()) {
                var lastMember = party.leader!!.getNMDData().lastMember.toInt()
                val stack = entityItem.toStack()
                stack.count = 1
                while (partyMembers.isNotEmpty() && !entityItem.isEmpty()){
                    if (lastMember >= partyMembers.size) lastMember = 0
                    val member = partyMembers[lastMember]
                    if (PlayerHelper.addDropsToPlayer(member, stack, true)){
                        entityItem.shrink()
                        lastMember++
                    }
                    else partyMembers.remove(member)
                }
                party.leader!!.getNMDData().lastMember = lastMember.toShort()
            }
            if (!entityItem.isEmpty())
                entityItem.spawnEntityPartyItem(party)
        }
    },
}

enum class SpecialLootSettingsEnum: ISpecialLootSettings {
    MasterLooter{
        override val width: Int
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

        override val height: Int
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

        override val recieveLastLootPosition: Boolean = false

        override fun getX(x: Int): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getY(y: Int): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override val renderOverGameplay: Boolean
            get() = false

        override val isListed: Boolean
            get() = false

        override fun renderLootWindow(gui: ItemRollGUI, sr: ScaledResolution, cursorX: Int, cursorY: Int, isFullRender: Boolean, clientData: ClientLootObject) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onClick(mc: Minecraft, cursorX: Int, cursorY: Int, state: Int, clientData: ClientLootObject): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun fromBytes(buf: ByteBuf) {
        }

        override fun toBytes(buf: ByteBuf, clientCache: Any?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun handleLoot(entityItem: SimpleEntityItem, party: IParty, cache: Any?) {

        }

        override fun processClientCache(player: EntityPlayer, clientCache: Any?, serverCache: Any?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun areConditionsMet(serverCache: Any?): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    },
    NeedBeforeGreed {
        override val width: Int = 158

        override val height: Int = 35

        override val recieveLastLootPosition: Boolean = false

        override fun getX(x: Int): Int {
            return width + 20
        }

        override fun getY(y: Int): Int {
            return if (y == -1) height + 20
            else y + height + 1
        }

        private val barWidth = 135
        private val barHeight = 10

        override val renderOverGameplay: Boolean
            get() = true

        override val isListed: Boolean
            get() = true

        override fun renderLootWindow(gui: ItemRollGUI, sr:ScaledResolution, cursorX: Int, cursorY: Int, isFullRender: Boolean, clientData: ClientLootObject) {
            val progress = (barWidth * (clientData.tickTime /(NMDConfig.loot.LootRollTimer * 20f))).toInt()
            gui.mc.renderEngine.bindTexture(ResourceLocation(NMDCore.MODID, "textures/gui/lootgui.png"))
            //Background
            val color = Color(clientData.rarity.rgb)
            GlStateManager.color(color.red.toFloat() / 255, color.green.toFloat() / 255, color.blue.toFloat() / 255)
            gui.drawTexturedModalRect(sr.scaledWidth - clientData.x, sr.scaledHeight - clientData.y, 0, 0, width, height)
            GlStateManager.color(1f, 1f, 1f)
            //Progress Bar
            gui.drawTexturedModalRect(sr.scaledWidth - clientData.x + (width - barWidth), sr.scaledHeight - clientData.y + 4, 0, 35, progress, barHeight)
            //Need Icon

            gui.drawTexturedModalRect(sr.scaledWidth - clientData.x + 100, sr.scaledHeight - clientData.y + 15, width, 0, 18, 18)
            //Greed Icon
            gui.drawTexturedModalRect(sr.scaledWidth - clientData.x + 116, sr.scaledHeight - clientData.y + 15, width, 18, 18, 18)
            //Pass Icon
            gui.drawTexturedModalRect(sr.scaledWidth - clientData.x + 132, sr.scaledHeight - clientData.y + 15, width + 18, 0, 18, 18)
            //Time remaining
            gui.mc.fontRenderer.drawString("${clientData.tickTime / 20}s", sr.scaledWidth - clientData.x + 50f, sr.scaledHeight - clientData.y + 16f, 0xFFFFFF, true)
            //Item render
            drawItemStack(clientData.stack, sr.scaledWidth - clientData.x + 4, sr.scaledHeight - clientData.y + 4)
            gui.mc.fontRenderer.drawString(clientData.stack.displayName, sr.scaledWidth - clientData.x + 24f, sr.scaledHeight - clientData.y + 5f, 0xFFFFFF, true)
            gui.mc.fontRenderer.drawString( "x${clientData.stack.count}", sr.scaledWidth - clientData.x + 24f, sr.scaledHeight - clientData.y + 16f, 0xFFFFFF, true)
        }

        override fun onClick(mc: Minecraft, cursorX: Int, cursorY: Int, state: Int, clientData: ClientLootObject): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun fromBytes(buf: ByteBuf) {
        }

        override fun toBytes(buf: ByteBuf, clientCache: Any?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun handleLoot(entityItem: SimpleEntityItem, party: IParty, cache: Any?) {

        }

        override fun createClientCache(stack: ItemStack, party: IParty): Any? {
            return 0
        }

        override fun createServerCache(party: IParty): Any? {
            val rollData = HashSet<RollData>()
            party.members.forEach {
                rollData.add(RollData(it.uniqueID))
            }
            return rollData
        }

        override fun processClientCache(player: EntityPlayer, clientCache: Any?, serverCache: Any?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        @Suppress("UNCHECKED_CAST")
        override fun areConditionsMet(serverCache: Any?): Boolean {
            return (serverCache as HashSet<RollData>).none { it.roll == 0f }
        }
    };
}