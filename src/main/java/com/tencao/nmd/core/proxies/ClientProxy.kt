package com.tencao.nmd.core.proxies

import com.tencao.nmd.core.events.listener.KeyPressListener
import com.tencao.nmd.core.gui.LootGUI
import com.tencao.nmd.core.util.ClientKeyHelper
import net.minecraftforge.common.MinecraftForge

class ClientProxy: IProxy {

    override fun registerKeyBinds() {
        ClientKeyHelper.registerMCBindings()
    }

    override fun registerEvents() {
        MinecraftForge.EVENT_BUS.register(LootGUI)
        MinecraftForge.EVENT_BUS.register(KeyPressListener)
    }
}