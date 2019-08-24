package com.tencao.nmd.proxies

import com.tencao.nmd.events.listener.KeyPressListener
import com.tencao.nmd.gui.LootGUI
import com.tencao.nmd.util.ClientKeyHelper
import net.minecraftforge.common.MinecraftForge

class ClientProxy: IProxy {

    override fun registerKeyBinds() {
        ClientKeyHelper.registerMCBindings()
    }

    override fun registerEvents() {
        MinecraftForge.EVENT_BUS.register(LootGUI)
        MinecraftForge.EVENT_BUS.register(KeyPressListener)
        super.registerEvents()
    }
}