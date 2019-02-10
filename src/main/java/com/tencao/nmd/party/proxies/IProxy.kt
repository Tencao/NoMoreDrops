package com.tencao.nmd.party.proxies

import com.tencao.nmd.party.events.listener.*
import net.minecraftforge.common.MinecraftForge

interface IProxy {

    fun registerEvents(){
        MinecraftForge.EVENT_BUS.register(LootRegisterListener)
        MinecraftForge.EVENT_BUS.register(PlayerEventListener)
        MinecraftForge.EVENT_BUS.register(DropEventListener)
        MinecraftForge.EVENT_BUS.register(PartyEventListener)
        MinecraftForge.EVENT_BUS.register(PlayerEventListener)
        MinecraftForge.EVENT_BUS.register(WorldEventListener)
    }
}