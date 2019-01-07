package com.tencao.nmd.api

import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.data.ClientLootObject
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.gui.LootGUI
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

/**
 * This is the main interface needed for creating custom loot settings.
 * If you're only doing server logic to distribute loot, this is the
 * interface you need.
 */
interface ILootSettings {

    /**
     * Display name for buttons
     */
    val displayName: String

    /**
     * This is called last once everything else has been processed
     * @param entityItem The data object containing the loot
     * @param party The party tied to the loot
     * @param serverCache The cache object, only present on ISpecialLoot
     * @return Returns the updated cache back, null if no changes
     */
    fun handleLoot(entityItem: SimpleEntityItem, party: IParty, serverCache: Any?): Any?

    /**
     * Creates a new server cache instance for the loot drop.
     */
    fun createServerCache(party: IParty): Any?{
        return null
    }

    /**
     * If true, this will store one cache per party and maintain it,
     * otherwise it will create a new cache everytime
     */
    fun persistentCache(): Boolean {
        return false
    }

}

/**
 * This is an extension of the main interface, allowing extra
 * functions like sending network data and rendering.
 */
interface ISpecialLootSettings: ILootSettings {

    /**
     * If false, a small notification icon will appear when
     * loot is ready to be rolled
     */
    val renderOverGameplay: Boolean

    /**
     * Is this element to be listed with other elements
     * or as a separate entity
     *
     * If true, the X and Y position of the render will
     * be preset based on the last displayed entry.
     */
    val isListed: Boolean

    /**
     * If set to true, this will send over the last X
     * and Y pos used by the same loot type, only useful
     * if isListed is false.
     */
    val recieveLastLootPosition: Boolean

    /**
     * Width and height of the loot screen window
     */
    val width: Int
    val height: Int

    /**
     * This is used to retrieve the position based on the
     * last X and Y of the previous element. If you have
     * recieveLastLootPosition, it will return the last
     * used position by the same loot type only. If you
     * have isListed instead, it will send the last
     * position of the listed elements, going down to up.
     *
     * This will be -1 if no previous entry is found
     */
    fun getX(x: Int): Int
    fun getY(y: Int): Int


    /**
     * This is called both during normal gameplay and when the
     * GUILootScreen is open
     * @param isFullRender False = Ingame, True = GUILootScreen
     * @param clientData The temp storage used by the loot setting
     */
    fun renderLootWindow(gui: LootGUI, sr: ScaledResolution, cursorX: Int, cursorY: Int, partialTicks: Float, isFullRender: Boolean, clientData: ClientLootObject)

    /**
     * This is called before onClick. If onClick doesn't return true
     * this will set the focus to this element, forwarding all keyboard
     * input to the element
     * @param cursorX Cursors X position onscreen
     * @param cursorY Cursors Y position onscreen
     * @param clientData The data object containing the cache,
     * stack, and remaining tick time
     * @return Return true if mouse is over, or false if not
     */
    fun isMouseOver(mc: Minecraft, cursorX: Int, cursorY: Int, clientData: ClientLootObject): Boolean

    /**
     * When a mouse clicks on the GUILootScreen
     * @param cursorX Cursors X position onscreen
     * @param cursorY Cursors Y position onscreen
     * @param state The mouse state
     * @param clientData The data object containing the cache,
     * stack, and remaining tick time
     * @return Return true to send a packet back to the server
     * containing the cache
     */
    fun onClick(mc: Minecraft, cursorX: Int, cursorY: Int, state: Int, clientData: ClientLootObject): Boolean {
        return false
    }

    /**
     * When a mouse clicks on the GUILootScreen
     * @param typedChar Character on the key
     * @param keyCode lwjgl Keyboard key code
     * @param clientData The data object containing the cache,
     * stack, and remaining tick time
     * @return Return true to send a packet back to the server
     * containing the cache
     */
    fun keyTyped(mc: Minecraft,typedChar: Char, keyCode: Int, clientData: ClientLootObject): Boolean {
        return false
    }

    /**
     * For creating a cache from transferred data
     */
    fun fromBytes(buf: ByteBuf): Any?

    /**
     * For writing data to send back to the server
     * @param clientCache The temp storage used by the loot setting
     */
    fun toBytes(buf: ByteBuf, clientCache: Any?)

    /**
     * Creates a new client cache instance for the loot drop.
     */
    fun createClientCache(stack: ItemStack, party: IParty): Any?{
        return null
    }

    /**
     * This is called after the player sends a packet back to
     * server.
     * @param player The player that sent the packet
     * @param clientCache The cache that was sent back
     * @param serverCache The server cache stored for the drop
     */
    fun processClientCache(player: EntityPlayer, clientCache: Any?, serverCache: Any?)

    /**
     * Checks if the conditions for the loot drop are complete
     * and ready to be processed
     */
    fun areConditionsMet(serverCache: Any?): Boolean



    /**
     * Copied from GuiContainer.class
     * Draws an ItemStack.
     *
     * The z index is increased by 32 (and not decreased afterwards), and the item is then rendered at z=200.
     */
    fun drawItemStack(stack: ItemStack, x: Int, y: Int) {
        if (stack.isEmpty) return
        val itemRenderer = LootGUI.mc.renderItem
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.translate(0.0f, 0.0f, 32.0f)
        val font: net.minecraft.client.gui.FontRenderer = stack.item.getFontRenderer(stack)?: LootGUI.mc.fontRenderer
        itemRenderer.zLevel = 200.0f
        itemRenderer.renderItemAndEffectIntoGUI(stack, x, y)
        itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y - 0, "")
        itemRenderer.zLevel = 0.0f
        RenderHelper.disableStandardItemLighting()
    }
}