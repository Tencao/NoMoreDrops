package com.tencao.nmd.gui

import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.data.ClientLootObject
import com.tencao.nmd.network.packets.LootServerPKT
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * This is the GUI that is called when starting a Need or Greed roll
 */
@Mod.EventBusSubscriber
object LootGUI : GuiScreen() {

    private var testStack: ItemStack = ItemStack.EMPTY
    private var lastFocus: ClientLootObject? = null

    init {
        mc = Minecraft.getMinecraft()
        testStack = ItemStack(Items.NETHER_STAR)
    }

    override fun drawScreen(cursorX: Int, cursorY: Int, partialTicks: Float) {
        draw(cursorX, cursorY, true)
    }



    fun draw (cursorX: Int, cursorY: Int, isFullRender: Boolean){
        // More accurate count of rendered objects vs index
        var count = 0
        val sr = ScaledResolution(mc)
        // Used to display a loot notification if loot filter has chosen not to render over gameplay
        var renderLootIcon = false
        mc.player.getNMDData().lootDrops.asSequence().forEachIndexed { _, entry ->
            if (!isFullRender && !entry.lootSetting.renderOverGameplay && !renderLootIcon) {
                renderLootIcon = true
            }
            else {
                if (!isFullRender && count >= 5) return@forEachIndexed
                entry.lootSetting.renderLootWindow(this, sr, cursorX, cursorY, isFullRender, entry)
                count++
            }

        }
    }

    /**
     * Calculates the screen position for each element based on
     * settings set in the ISpecialLootSettings.
     */
    fun calculateXY(lootObject: ClientLootObject){
        if (mc.player.getNMDData().lootDrops.isEmpty()){
            if (lootObject.lootSetting.isListed)
                lootObject.setXY(lootObject.lootSetting.width + 20, lootObject.lootSetting.height + 20)
            else
                lootObject.setXY(lootObject.lootSetting.getX(-1), lootObject.lootSetting.getY(-1))
        }
        else {
            val index = mc.player.getNMDData().lootDrops.indexOf(lootObject)
            if (lootObject.lootSetting.isListed) {
                mc.player.getNMDData().lootDrops.asSequence().filterIndexed { pos, _ -> pos < index }.findLast { it.lootSetting.isListed }?.let {
                    lootObject.setXY(lootObject.lootSetting.getX(it.x), lootObject.lootSetting.getY(it.y))
                } ?: lootObject.setXY(lootObject.lootSetting.width + 20, lootObject.lootSetting.height + 20)
            } else if (lootObject.lootSetting.recieveLastLootPosition) {
                mc.player.getNMDData().lootDrops.asSequence().filterIndexed { pos, _ -> pos < index }.findLast { it.lootSetting == lootObject.lootSetting }?.let {
                    lootObject.setXY(lootObject.lootSetting.getX(it.x), lootObject.lootSetting.getY(it.y))
                } ?: lootObject.setXY(lootObject.lootSetting.getX(-1), lootObject.lootSetting.getY(-1))
            }
        }
    }

    /**
     * Recalculates the position of all elements past the
     * element removed.
     *
     * @param lootSettings The loot settings used
     *
     */
    fun recalculateFrom(lootSettings: ISpecialLootSettings){
        mc.player
                .getNMDData()
                .lootDrops
                .asSequence()
                .filter { if (lootSettings.isListed) it.lootSetting.isListed else it.lootSetting == lootSettings }
                .forEachIndexed { _, clientLootObject -> calculateXY(clientLootObject) }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        mc.player.getNMDData().lootDrops.firstOrNull { it.lootSetting.onClick(mc, mouseX, mouseY, state, it) }?.let {
            mc.player.getNMDData().lootDrops.remove(it)
            PacketPipeline.sendToServer(LootServerPKT(it.rollID, it.lootSetting, it.clientCache))
            if (it.lootSetting.isListed || it.lootSetting.recieveLastLootPosition)
                recalculateFrom(it.lootSetting)
        }
        lastFocus = mc.player.getNMDData().lootDrops.firstOrNull { mouseX >= it.x && mouseX <= it.x + it.lootSetting.width && mouseY >= it.y && mouseY <= it.y + it.lootSetting.height }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        lastFocus?.lootSetting?.keyTyped(mc, typedChar, keyCode, lastFocus!!)
        super.keyTyped(typedChar, keyCode)
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    @SubscribeEvent
    fun renderOverlay(event: RenderGameOverlayEvent){
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT && mc.currentScreen != this) {
            draw(0, 0, false)
        }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
    }

}