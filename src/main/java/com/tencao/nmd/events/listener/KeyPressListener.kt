package com.tencao.nmd.events.listener

import com.tencao.nmd.gui.LootGUI
import com.tencao.nmd.util.ClientKeyHelper
import com.tencao.nmd.util.Keybinds
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object KeyPressListener{

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent){
        ClientKeyHelper.mcToNmd.keys.firstOrNull { it.isKeyDown }?.let {
            if (it.keyDescription == Keybinds.LOOT_GUI.keyName)
                Minecraft.getMinecraft().displayGuiScreen(LootGUI)
        }
    }
}