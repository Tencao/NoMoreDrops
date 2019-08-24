package com.tencao.nmd

import be.bluexin.saomclib.party.IParty
import be.bluexin.saomclib.party.IPlayerInfo
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.gui.LootGUI
import com.tencao.nmd.util.PlayerHelper
import com.tencao.nmd.data.ClientLootObject
import com.tencao.nmd.data.RollData
import com.tencao.nmd.data.SimpleEntityItem
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fml.common.FMLCommonHandler
import java.awt.Color

enum class LootSettingsEnum: ILootSettings {
    None {
        override val displayName: String
            get() = "NONE"

        override fun handleLoot(entityItem: SimpleEntityItem, party: IParty, serverCache: Any?): Any? {
            entityItem.spawnEntityPartyItem(party, true)
            return null
        }
    },
    Random {

        override val displayName: String = "Random"

        override fun handleLoot(entityItem: SimpleEntityItem, party: IParty, serverCache: Any?) {
            val partyMembers = getNearbyParty(entityItem, party)
            if (partyMembers.isNotEmpty()) {
                val stack = entityItem.toStack()
                stack.count = 1
                while (partyMembers.isNotEmpty() && !entityItem.isEmpty()){
                    val member = partyMembers[NMDCore.rand.nextInt(partyMembers.size - 1)]
                    if (PlayerHelper.addDropsToPlayer(member, stack, true)){
                        entityItem.shrink()
                    }
                    else partyMembers.remove(member)
                }
            }
            if (!entityItem.isEmpty())
                entityItem.spawnEntityPartyItem(party, true)
        }
    },
    RoundRobin{

        override val displayName: String = "Round Robin"

        override fun handleLoot(entityItem: SimpleEntityItem, party: IParty, serverCache: Any?): Any? {
            val partyMembers = getNearbyParty(entityItem, party)
            var lastMember = serverCache as Int

            if (partyMembers.isNotEmpty()) {
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
            }
            if (!entityItem.isEmpty())
                entityItem.spawnEntityPartyItem(party, true)
            return lastMember
        }

        override fun createServerCache(party: IParty): Int {
            return 0
        }

        override fun persistentCache(): Boolean {
            return true
        }
    };

    fun getNearbyParty(entityItem: SimpleEntityItem, party: IParty): MutableList<EntityPlayer>{
        return party.membersInfo.mapNotNull(IPlayerInfo::player).filter { pl -> !pl.getNMDData().isBlackListed(entityItem.toStack()) && entityItem.getDistanceSq(pl) <= PlayerHelper.squareSum(64) }.toMutableList()
    }
}

enum class SpecialLootSettingsEnum: ISpecialLootSettings {
    MasterLooter{

        override val displayName: String = "Master Looter"

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

        override fun renderLootWindow(gui: LootGUI, sr: ScaledResolution, cursorX: Int, cursorY: Int, partialTicks: Float, isFullRender: Boolean, clientData: ClientLootObject) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun fromBytes(buf: ByteBuf) {
        }

        override fun toBytes(buf: ByteBuf, clientCache: Any?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun handleLoot(entityItem: SimpleEntityItem, party: IParty, serverCache: Any?) {

        }

        override fun isMouseOver(mc: Minecraft, cursorX: Int, cursorY: Int, clientData: ClientLootObject): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun processClientCache(player: EntityPlayer, clientCache: Any?, serverCache: Any?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun areConditionsMet(serverCache: Any?): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    },
    NeedOrGreed {

        override val displayName: String = "Need or Greed"

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
        private val needButton = 100
        private val greedButton = 116
        private val passButton = 132

        override val renderOverGameplay: Boolean
            get() = true

        override val isListed: Boolean
            get() = true

        override fun renderLootWindow(gui: LootGUI, sr:ScaledResolution, cursorX: Int, cursorY: Int, partialTicks: Float, isFullRender: Boolean, clientData: ClientLootObject) {
            val progress = (barWidth * (clientData.tickTime /(NMDConfig.partycfg.LootRollTimer * 20f))).toInt()
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
            drawItemStack(clientData.simpleStack.stack, sr.scaledWidth - clientData.x + 4, sr.scaledHeight - clientData.y + 4)
            gui.mc.fontRenderer.drawString(clientData.simpleStack.stack.displayName, sr.scaledWidth - clientData.x + 24f, sr.scaledHeight - clientData.y + 5f, 0xFFFFFF, true)
            gui.mc.fontRenderer.drawString( "x${clientData.simpleStack.count}", sr.scaledWidth - clientData.x + 24f, sr.scaledHeight - clientData.y + 16f, 0xFFFFFF, true)
        }

        override fun isMouseOver(mc: Minecraft, cursorX: Int, cursorY: Int, clientData: ClientLootObject): Boolean {
            val sr = ScaledResolution(mc)
            val y = sr.scaledHeight - clientData.y
            val x = sr.scaledWidth - clientData.x
            return cursorX >= x && cursorX <= x + width && cursorY >= y && cursorY <= y + height
        }

        override fun onClick(mc: Minecraft, cursorX: Int, cursorY: Int, state: Int, clientData: ClientLootObject): Boolean {
            val sr = ScaledResolution(mc)
            val y = sr.scaledHeight - clientData.y
            val x = sr.scaledWidth - clientData.x
            if (cursorY >= y + 15 && cursorY <= y + 33){
                if (cursorX >= x + needButton && cursorX <= x + needButton + 18){
                    clientData.clientCache = 2
                    return true
                }
                else if (cursorX >= x + greedButton && cursorX <= x + greedButton + 18){
                    clientData.clientCache = 1
                    return true
                }
                else if (cursorX >= x + passButton && cursorX <= x + passButton + 18){
                    clientData.clientCache = 0
                    return true
                }
            }
            return false
        }

        override fun fromBytes(buf: ByteBuf): Any? {
            return buf.readInt()
        }

        override fun toBytes(buf: ByteBuf, clientCache: Any?) {
            buf.writeInt(clientCache as Int)
        }

        @Suppress("UNCHECKED_CAST")
        override fun handleLoot(entityItem: SimpleEntityItem, party: IParty, serverCache: Any?) {
            val winner = (serverCache as HashSet<RollData>)
                    .asSequence()
                    .filter ( RollData::isRollValid )
                    .sortedWith( compareByDescending { it.roll } )
                    .firstOrNull { rollData -> PlayerHelper.addDropsToPlayer(rollData.uuid, entityItem.toStack(), false) }
            if (winner == null) {
                serverCache.forEach {
                    FMLCommonHandler.instance().minecraftServerInstance.playerList.players.firstOrNull { player -> player.uniqueID == it.uuid }?.sendMessage(TextComponentTranslation("nmd.loot.needorgreed.noroll", entityItem.toStack().displayName))
                }
                entityItem.spawnEntityPartyItem(party, true)
            }
            else {
                serverCache.asSequence().filter ( RollData::isRollValid ).forEach {
                    if (it == winner) {
                        val rolltype = if (it.getRoll().second == 2.toByte()) "Need" else "Greed"
                        FMLCommonHandler.instance().minecraftServerInstance.playerList.players.firstOrNull { player -> player.uniqueID == it.uuid }?.sendMessage(TextComponentTranslation("nmd.loot.needorgreed.won", entityItem.toStack().displayName, rolltype, it.getRoll().first))
                    } else {
                        val winningRoll = winner.getRoll().first
                        val winningPlayer = FMLCommonHandler.instance().minecraftServerInstance.playerList.players.first { player -> player.uniqueID == winner.uuid }
                        val rolltype = if (winner.getRoll().second == 2.toByte()) "Need" else "Greed"
                        FMLCommonHandler.instance().minecraftServerInstance.playerList.players.firstOrNull { player -> player.uniqueID == it.uuid }?.sendMessage(TextComponentTranslation("nmd.loot.needorgreed.roll", winningPlayer.displayNameString, entityItem.toStack().displayName, rolltype, winningRoll))
                    }
                }
            }
        }

        override fun createClientCache(stack: ItemStack, party: IParty): Any? {
            return 0
        }

        override fun createServerCache(party: IParty): Any? {
            val rollData = HashSet<RollData>()
            party.membersInfo.mapNotNull(IPlayerInfo::player).forEach {
                rollData.add(RollData(it.uniqueID))
            }
            return rollData
        }

        @Suppress("UNCHECKED_CAST")
        override fun processClientCache(player: EntityPlayer, clientCache: Any?, serverCache: Any?) {
            (serverCache as HashSet<RollData>).firstOrNull { it.test(player) }?.roll(clientCache as Int)
        }

        @Suppress("UNCHECKED_CAST")
        override fun areConditionsMet(serverCache: Any?): Boolean {
            return (serverCache as HashSet<RollData>).none { it.roll == 0 }
        }

    };
}