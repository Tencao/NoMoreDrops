package com.tencao.nmd.proxies

import com.tencao.nmd.events.*
import net.minecraftforge.common.MinecraftForge

interface IProxy {

    fun registerKeyBinds(){}

    fun registerEvents(){
        MinecraftForge.EVENT_BUS.register(PlayerEventListener)
        MinecraftForge.EVENT_BUS.register(PartyEventListener)
        MinecraftForge.EVENT_BUS.register(WorldEventListener)
        MinecraftForge.EVENT_BUS.register(BlockEventListener)
        MinecraftForge.EVENT_BUS.register(LivingEventListener)
        MinecraftForge.EVENT_BUS.register(EntityItemEventListener)
    }
}