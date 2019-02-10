package com.tencao.nmd.core.events.listener

import com.tencao.nmd.core.gui.LootGUI
import com.tencao.nmd.core.util.ClientKeyHelper
import com.tencao.nmd.core.util.Keybinds
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object KeyPressListener{

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent){
        ClientKeyHelper.mcToNmd.asSequence().firstOrNull { it.key.isKeyDown }?.let {
            if (it.key.keyDescription == Keybinds.LOOT_GUI.keyName)
                Minecraft.getMinecraft().displayGuiScreen(LootGUI)
        }
    }
}