package com.tencao.nmd.party

import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.core.NMDCore
import com.tencao.nmd.party.network.packets.LootClientPKT
import com.tencao.nmd.party.network.packets.LootServerPKT
import com.tencao.nmd.party.network.packets.LootSyncPKT
import com.tencao.nmd.party.PartyCore.DEPS
import com.tencao.nmd.party.PartyCore.MODID
import com.tencao.nmd.party.PartyCore.NAME
import com.tencao.nmd.party.events.handler.RegisterLootRarityEvent
import com.tencao.nmd.party.proxies.IProxy
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

@Mod(modid = MODID, name = NAME, version = NMDCore.VERSION, dependencies = DEPS)
object PartyCore {
    const val MODID = "nmd-party"
    const val NAME = "No More Drops - Party Module"
    const val DEPS = "required-after:${NMDCore.MODID}"

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this

    @SidedProxy(clientSide = "com.tencao.nmd.party.proxies.ClientProxy", serverSide = "com.tencao.nmd.party.proxies.ServerProxy", modId = MODID)
    lateinit var proxy: IProxy

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        // ========== Register Events ==========
        PartyCore.proxy.registerEvents()

        // ========== Register Network ==========
        PacketPipeline.registerMessage(LootClientPKT::class.java, LootClientPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(LootServerPKT::class.java, LootServerPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(LootSyncPKT::class.java, LootSyncPKT.Companion.Handler::class.java)
    }

    @Mod.EventHandler
    fun loadFinished(event: FMLLoadCompleteEvent){
        MinecraftForge.EVENT_BUS.post(RegisterLootRarityEvent())
    }
}