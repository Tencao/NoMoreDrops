package com.tencao.nmd.core.proxies

import com.tencao.nmd.core.events.listener.LivingDeathListener
import net.minecraftforge.common.MinecraftForge

interface IProxy {

    fun registerKeyBinds(){}

    fun registerEvents(){
        MinecraftForge.EVENT_BUS.register(LivingDeathListener)
    }
}