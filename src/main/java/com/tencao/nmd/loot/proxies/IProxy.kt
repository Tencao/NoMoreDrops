package com.tencao.nmd.loot.proxies

import com.tencao.nmd.loot.events.listener.BlockEventListener
import com.tencao.nmd.loot.events.listener.EntityItemEventListener
import com.tencao.nmd.loot.events.listener.DropEventListener
import net.minecraftforge.common.MinecraftForge

interface IProxy {

    fun registerEvents() {
        MinecraftForge.EVENT_BUS.register(BlockEventListener)
        MinecraftForge.EVENT_BUS.register(DropEventListener)
        MinecraftForge.EVENT_BUS.register(EntityItemEventListener)
    }
}