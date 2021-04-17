package com.tencao.nmd

import be.bluexin.saomclib.party.IPartyData
import be.bluexin.saomclib.party.PlayerInfo
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.config.NMDConfig
import com.tencao.nmd.data.ClientLootObject
import com.tencao.nmd.data.RollData
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.gui.LootGUI
import com.tencao.nmd.util.PlayerHelper
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
import java.util.*
import kotlin.collections.HashSet

enum class LootSettingsEnum: ILootSettings {
    None {
        override val displayName: String
            get() = "NONE"

        override fun handleLoot(entityItem: SimpleEntityItem, party: List<PlayerInfo>, serverCache: Any?): Any? {
            entityItem.spawnEntityPartyItem(party, true)
            return null
        }
    },
    Random {

        override val displayName: String = "Random"

        override fun handleLoot(entityItem: SimpleEntityItem, party: List<PlayerInfo>, serverCache: Any?) {
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

        override fun handleLoot(entityItem: SimpleEntityItem, party: List<PlayerInfo>, serverCache: Any?): Any? {
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

        override fun createServerCache(party: List<PlayerInfo>): Int {
            return 0
        }

        override fun persistentCache(): Boolean {
            return true
        }
    };

    fun getNearbyParty(entityItem: SimpleEntityItem, party: List<PlayerInfo>): MutableList<EntityPlayer>{
        return party.map { it.player }.filterIsInstance<EntityPlayer>().filter { player ->
            player.getNMDData().isBlackListed(entityItem.toStack()) && entityItem.getDistanceSq(player) <= PlayerHelper.squareSum(64)
        }.toMutableList()
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

        override fun isMouseOver(mc: Minecraft, sr: ScaledResolution, cursorX: Int, cursorY: Int, clientData: ClientLootObject): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun fromBytes(buf: ByteBuf) {
        }

        override fun toBytes(buf: ByteBuf, clientCache: Any?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun handleLoot(entityItem: SimpleEntityItem, party: List<PlayerInfo>, serverCache: Any?) {

        }


        override fun processClientCache(player: EntityPlayer, clientCache: Any?, serverCache: Any?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun areConditionsMet(serverCache: Any?): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun shouldSendToClient(player: EntityPlayer, serverCache: Any?): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    },
    NeedOrGreed {

        private val RES_ITEM_GLINT = ResourceLocation("textures/misc/enchanted_item_glint.png")

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
            val x = sr.scaledWidth - clientData.x
            val y = sr.scaledHeight - clientData.y
            val color = Color(clientData.rarity.rgb)

            GlStateManager.disableLighting()
            GlStateManager.color(color.red.toFloat() / 255, color.green.toFloat() / 255, color.blue.toFloat() / 255)
            gui.drawTexturedModalRect(x, y, 0, 0, width, height)
            GlStateManager.color(1f, 1f, 1f)

            if (clientData.rarity.hasEffect) {
                gui.mc.renderEngine.bindTexture(RES_ITEM_GLINT)
                GlStateManager.depthMask(false)
                GlStateManager.depthFunc(514)
                GlStateManager.enableBlend()
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE)
                GlStateManager.matrixMode(5890)
                GlStateManager.pushMatrix()
                GlStateManager.scale(8.0f, 8.0f, 8.0f)
                val f = (Minecraft.getSystemTime() % 3000L).toFloat() / 3000.0f / 8.0f
                GlStateManager.translate(f, 0.0f, 0.0f)
                GlStateManager.rotate(-50.0f, 0.0f, 0.0f, 1.0f)
                gui.drawTexturedModalRect(x, y, 0, 0, width, height)
                GlStateManager.popMatrix()
                GlStateManager.pushMatrix()
                GlStateManager.scale(8.0f, 8.0f, 8.0f)
                val f1 = (Minecraft.getSystemTime() % 4873L).toFloat() / 4873.0f / 8.0f
                GlStateManager.translate(-f1, 0.0f, 0.0f)
                GlStateManager.rotate(10.0f, 0.0f, 0.0f, 1.0f)
                gui.drawTexturedModalRect(x, y, 0, 0, width, height)
                GlStateManager.popMatrix()
                GlStateManager.matrixMode(5888)
                GlStateManager.disableBlend()
                GlStateManager.depthFunc(515)
                GlStateManager.depthMask(true)
                gui.mc.renderEngine.bindTexture(ResourceLocation(NMDCore.MODID, "textures/gui/lootgui.png"))
            }
            //Progress Bar
            gui.drawTexturedModalRect(x + (width - barWidth), y + 4, 0, 35, progress, barHeight)
            //Need Icon
            gui.drawTexturedModalRect(x + needButton, y + 15, width, 0, 18, 18)
            //Greed Icon
            gui.drawTexturedModalRect(x + greedButton, y + 15, width, 18, 18, 18)
            //Pass Icon
            gui.drawTexturedModalRect(x + passButton, y + 15, width + 18, 0, 18, 18)
            //Time remaining
            gui.mc.fontRenderer.drawString("${clientData.tickTime / 20}s", x + 50f, y + 16f, 0xFFFFFF, true)
            //Item render
            drawItemStack(clientData.simpleStack.stack, x + 4, y + 4, cursorX, cursorY)
            val name = clientData.simpleStack.stack.displayName
            gui.mc.fontRenderer.drawString(if (name.length > 24) name.substring(0, 24) else name , x + 24f, y + 5f, 0xFFFFFF, true)
            gui.mc.fontRenderer.drawString( "x${clientData.simpleStack.count}", x + 24f, y + 16f, 0xFFFFFF, true)
            if (IntRange(y + 4, y + 20).contains(cursorY) && IntRange(x + 4, x + 20).contains(cursorX))
                renderItemToolTip(clientData.simpleStack.stack, cursorX, cursorY, sr.scaledWidth - width, sr.scaledHeight)

            // Ensure lighting is reset upon completion
            GlStateManager.disableLighting()
        }

        override fun isMouseOver(mc: Minecraft, sr: ScaledResolution, cursorX: Int, cursorY: Int, clientData: ClientLootObject): Boolean {
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
        override fun handleLoot(entityItem: SimpleEntityItem, party: List<PlayerInfo>, serverCache: Any?) {
            val winner = (serverCache as HashSet<RollData>)
                    .asSequence()
                    .filter ( RollData::isRollValid )
                    .sortedWith( compareByDescending { it.roll } )
                    .firstOrNull { rollData -> PlayerHelper.addDropsToPlayer(rollData.playerInfo, entityItem.toStack(), false) }
            if (winner == null) {
                serverCache.forEach { rollData ->
                    rollData.playerInfo.player?.sendMessage(TextComponentTranslation("nmd.loot.needorgreed.noroll", entityItem.toStack().displayName))
                }
                entityItem.spawnEntityPartyItem(party, true)
            }
            else {
                serverCache.asSequence().filter ( RollData::isRollValid ).forEach { rollData ->
                    if (rollData == winner) {
                        val rolltype = if (rollData.getRoll().second == 2.toByte()) "Need" else "Greed"
                        rollData.playerInfo.player?.sendMessage(TextComponentTranslation("nmd.loot.needorgreed.won", entityItem.toStack().displayName, rolltype, rollData.getRoll().first))
                    } else {
                        val winningRoll = winner.getRoll().first
                        val winningPlayer = winner.playerInfo
                        val rolltype = if (winner.getRoll().second == 2.toByte()) "Need" else "Greed"
                        rollData.playerInfo.player?.sendMessage(TextComponentTranslation("nmd.loot.needorgreed.roll", winningPlayer.username, entityItem.toStack().displayName, rolltype, winningRoll))
                    }
                }
            }
        }

        override fun createClientCache(stack: ItemStack, party: IPartyData): Any? {
            return 0
        }

        override fun createServerCache(party: List<PlayerInfo>): Any? {
            val rollData = HashSet<RollData>()
            party.forEach {
                rollData.add(RollData(it))
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

        @Suppress("UNCHECKED_CAST")
        override fun shouldSendToClient(player: EntityPlayer, serverCache: Any?): Boolean {
            return (serverCache as HashSet<RollData>).firstOrNull { it.test(player) }?.hasRolled() == false
        }

    };
}