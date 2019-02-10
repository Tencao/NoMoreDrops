package com.tencao.nmd.core.gui

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler

object GuiHandler : IGuiHandler {

    private const val BLACKLIST_GUI = 0
    private const val ITEMROLL_GUI = 1

    override fun getClientGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        return when(ID){
            BLACKLIST_GUI -> BlackListGUI
            ITEMROLL_GUI -> LootGUI
            else -> null
        }
    }

    override fun getServerGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        return null
    }
}