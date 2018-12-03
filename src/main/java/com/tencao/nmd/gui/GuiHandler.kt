package com.tencao.nmd.gui

import com.tencao.nmd.capability.getNMDData
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler

object GuiHandler : IGuiHandler {

    private const val BLACKLIST_GUI = 0

    override fun getClientGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        if (ID == BLACKLIST_GUI)
            return BlackListGUI(player!!.inventory, player.getNMDData().getItemList())
        return null
    }

    override fun getServerGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        return null
    }
}