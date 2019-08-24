package com.tencao.nmd.gui

import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.network.packets.BlackListPKT
import net.minecraft.client.gui.GuiScreen

object BlackListGUI: GuiScreen(){

    override fun onGuiClosed() {
        PacketPipeline.sendToServer(BlackListPKT(mc.player.getNMDData().itemBlacklist))
        super.onGuiClosed()
    }
}