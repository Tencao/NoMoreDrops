package com.tencao.nmd.proxies

import com.tencao.nmd.events.listener.*
import net.minecraftforge.common.MinecraftForge

interface IProxy {

    fun registerKeyBinds(){}

    fun registerEvents(){
        MinecraftForge.EVENT_BUS.register(LivingDeathListener)
        MinecraftForge.EVENT_BUS.register(BlockEventListener)
        MinecraftForge.EVENT_BUS.register(DropEventListener)
        MinecraftForge.EVENT_BUS.register(EntityItemEventListener)
        MinecraftForge.EVENT_BUS.register(LootRegisterListener)
        MinecraftForge.EVENT_BUS.register(PlayerEventListener)
        MinecraftForge.EVENT_BUS.register(PartyEventListener)
        MinecraftForge.EVENT_BUS.register(WorldEventListener)
    }
}