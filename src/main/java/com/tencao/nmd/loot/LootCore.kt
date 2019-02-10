package com.tencao.nmd.loot

import com.tencao.nmd.core.NMDCore
import com.tencao.nmd.loot.LootCore.DEPS
import com.tencao.nmd.loot.LootCore.MODID
import com.tencao.nmd.loot.LootCore.NAME
import com.tencao.nmd.loot.proxies.IProxy
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

@Mod(modid = MODID, name = NAME, version = NMDCore.VERSION, dependencies = DEPS)
object LootCore {
    const val MODID = "nmd-loot"
    const val NAME = "No More Drops - Loot Module"
    const val DEPS = "required-after:${NMDCore.MODID}"

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this

    @SidedProxy(clientSide = "com.tencao.nmd.loot.proxies.ClientProxy", serverSide = "com.tencao.nmd.loot.proxies.ServerProxy", modId = MODID)
    lateinit var proxy: IProxy

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        // ========== Register Events ==========
        LootCore.proxy.registerEvents()
    }
}