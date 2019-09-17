package com.tencao.nmd.gui

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.NMDCore
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.gui.buttons.GUILootButton
import com.tencao.nmd.network.packets.LootServerPKT
import com.tencao.nmd.network.packets.LootSyncPKT
import com.tencao.nmd.util.GUIScreenReflect
import com.tencao.nmd.util.ModHelper
import com.tencao.nmd.util.PartyHelper
import com.tencao.nmd.data.ClientLootObject
import com.tencao.nmd.registry.LootRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.toasts.GuiToast
import net.minecraft.client.resources.I18n
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse

/**
 * This is the GUI that is called when starting a Need or Greed roll
 */
object LootGUI : GuiScreen() {

    private var lastFocus: ClientLootObject? = null
    private lateinit var buttons: ArrayList<GuiButton>
    var lastMouseX: Int = 0
    var lastMouseY: Int = 0
    var hasClicked: Boolean = false

    init {
        mc = Minecraft.getMinecraft()
    }

    override fun initGui() {
        super.initGui()
        val buttons = arrayListOf<GuiButton>()

        val sr = ScaledResolution(mc)
        buttons.add(GuiButton(0, 20, 10, 150, 20, I18n.format("nmd.lootgui.settings")))

        buttons.add(GuiButton(1, 200, 10, 150, 20, I18n.format("nmd.lootgui.party")))
        buttons[1].visible = !ModHelper.isSAOUILoaded
        buttons.add(GuiButton(2, (sr.scaledWidth / 2) - 75, (sr.scaledHeight / 2) - 42, 150, 20, I18n.format("nmd.pt.invite")))
        buttons.add(GuiButton(3, (sr.scaledWidth / 2) - 75, sr.scaledHeight / 2 - 21, 150, 20, I18n.format("nmd.pt.leave")))
        buttons.add(GuiButton(4, (sr.scaledWidth / 2) - 75, (sr.scaledHeight / 2), 150, 20, I18n.format("nmd.pt.kick")))
        buttons.add(GuiButton(5, (sr.scaledWidth / 2) - 75, (sr.scaledHeight / 2) + 21, 150, 20, I18n.format("nmd.pt.disband")))
        buttons.subList(2, 6).forEach { it.visible = false }
        buttons.subList(4, 6).forEach { it.enabled = mc.player.getPartyCapability().getOrCreatePT().isLeader(mc.player) }
        var count = 0
        mc.player.getNMDData().lootSettings.forEach { (rarity, lootsetting) ->
            val button = GUILootButton(rarity, lootsetting, 5 + ++count, (sr.scaledWidth / 2) - 75, (sr.scaledHeight / 2) - 70 + (21 * count), 150, 20)
            button.visible = false
            buttons.add(button)
        }

        LootGUI.buttons = buttons

    }

    override fun drawScreen(cursorX: Int, cursorY: Int, partialTicks: Float) {
        draw(ScaledResolution(mc), cursorX, cursorY, partialTicks, true)
    }


    fun draw (sr: ScaledResolution, cursorX: Int, cursorY: Int, partialTicks: Float, isFullRender: Boolean){
        // More accurate count of rendered objects vs index
        var count = 0
        // Used to display a loot notification if loot filter has chosen not to render over gameplay
        var renderLootIcon = false
        mc.player.getNMDData().lootDrops.asSequence().forEachIndexed { _, entry ->
            if (!isFullRender && !entry.lootSetting.renderOverGameplay && !renderLootIcon) {
                renderLootIcon = true
            }
            else {
                if (!isFullRender && count >= 5) return@forEachIndexed
                entry.lootSetting.renderLootWindow(this, sr, cursorX, cursorY, partialTicks, isFullRender, entry)
                count++
            }

        }
        if (isFullRender)
            buttons.forEach { it.drawButton(mc, cursorX, cursorY, partialTicks) }

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

    override fun mouseReleased(cursorX: Int, cursorY: Int, state: Int) {
        buttons.firstOrNull { it.mousePressed(mc, cursorX, cursorY) }?.run {
            when (this.id) {
                0 -> {
                    if (buttons[2].visible)
                        buttons.subList(2, 6).forEach { it.visible = false }
                    if (buttons[6].visible)
                        buttons.subList(6, buttons.size).forEach { it.visible = false }
                    else
                        buttons.subList(6, buttons.size).forEach { it.visible = true }
                }
                1 -> {
                    buttons.subList(6, buttons.size).forEach { it.visible = false }
                    if (buttons[2].visible)
                        buttons.subList(2, 6).forEach { it.visible = false }
                    else {
                        buttons[2].visible = true
                        if (PartyHelper.isValidParty(mc.player)) {
                            buttons[3].visible = true
                            if (mc.player.getPartyCapability().getOrCreatePT().leaderInfo?.player == mc.player) {
                                buttons[4].visible = true
                                buttons[5].visible = true
                            } else return@run
                        } else return@run
                    }
                }
                2 -> {
                    //TODO Add party invite function
                }
                3 -> mc.player.getPartyCapability().getOrCreatePT().removeMember(mc.player)
                4 -> mc.player.getPartyCapability().getOrCreatePT().dissolve()
                5 -> {
                    //TODO Add party kick function
                }

                else -> {
                    if (mc.player.getPartyCapability().getOrCreatePT().isLeader(mc.player)) {
                        if (this is GUILootButton) {
                            var lootSetting = this.lootSetting
                            lootSetting = LootRegistry.getNextLootSetting(lootSetting)
                            PacketPipeline.sendToServer(LootSyncPKT(this.rarity, lootSetting))
                            this.updateLootSetting(lootSetting)
                        }
                    }
                }
            }
        }
        lootClick(mc, ScaledResolution(mc), cursorX, cursorY, state)
    }

    fun lootClick(mc: Minecraft, sr: ScaledResolution, cursorX: Int, cursorY: Int, state: Int){
        mc.player.getNMDData().lootDrops
                .firstOrNull { it.lootSetting.isMouseOver(LootGUI.mc, sr, cursorX, cursorY, it) }
                ?.run {
                    if (this.lootSetting.onClick(LootGUI.mc, cursorX, cursorY, state, this)) {
                        mc.player.getNMDData().lootDrops.remove(this)
                        PacketPipeline.sendToServer(LootServerPKT(this.rollID, this.lootSetting, this.clientCache))
                        if (this.lootSetting.isListed || this.lootSetting.recieveLastLootPosition)
                            recalculateFrom(this.lootSetting)

                        lastFocus = null

                        // Checks to see if an element was clicked, if so, reset focus and check remaining loot entries.
                        // If none are left, close the GUIScreen
                        if (LootGUI.mc.player.getNMDData().lootDrops.isEmpty()) {
                            this@LootGUI.mc.displayGuiScreen(null)

                            if (this@LootGUI.mc.currentScreen == null) {
                                this@LootGUI.mc.setIngameFocus()
                            }
                        }
                    } else {
                        lastFocus = this
                    }
                }
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
        val sr = ScaledResolution(mc)
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT && mc.currentScreen != this) {
            if (mc.currentScreen != null) {
                val cursorX = Mouse.getEventX() / 2
                val cursorY = (mc.displayHeight - Mouse.getY()) / 2
                lastMouseX = cursorX
                lastMouseY = cursorY
                //cursorY = mc.currentScreen!!.height - Mouse.getY() * mc.currentScreen!!.height / sr.scaledHeight - 1
            }

            draw(sr, lastMouseX, lastMouseY, 0f, false)
        }

        if (mc.player?.getNMDData()?.lootDrops?.isNotEmpty() == true) {
            val screen = mc.currentScreen ?: return
            if (!mc.inGameHasFocus && screen !is LootGUI) {
                val state = Mouse.getEventButton()
                if (state == 0) {
                    if (!hasClicked) {
                        var touchValue = GUIScreenReflect.getTouchValue(screen)
                        if (mc.gameSettings.touchscreen && --touchValue > 0) {
                            return
                        }
                        hasClicked = true

                        lootClick(mc, sr, lastMouseX, lastMouseY, state)
                    }
                } else {
                    hasClicked = false
                }

            }
        }
    }

}