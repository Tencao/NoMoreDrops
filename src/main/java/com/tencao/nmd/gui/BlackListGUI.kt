package com.tencao.nmd.gui

import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.network.packets.BlackListPKT
import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

class BlackListGUI(val inv: InventoryPlayer, val blackList: NonNullList<ItemStack>): GuiScreen(){

    override fun onGuiClosed() {
        PacketPipeline.sendToServer(BlackListPKT(blackList))
        super.onGuiClosed()
    }
}