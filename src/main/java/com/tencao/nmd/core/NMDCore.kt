package com.tencao.nmd.core

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.core.capability.PlayerData
import com.tencao.nmd.core.gui.GuiHandler
import com.tencao.nmd.core.util.PlayerHelper
import com.tencao.nmd.core.network.commands.NMDCommand
import com.tencao.nmd.core.network.packets.BlackListPKT
import com.tencao.nmd.core.network.packets.SoundPKT
import com.tencao.nmd.core.proxies.IProxy
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(modid = NMDCore.MODID, name = NMDCore.NAME, version = NMDCore.VERSION, dependencies = NMDCore.DEPS)
object NMDCore {
    const val MODID = "nmd"
    const val NAME = "No More Drops"
    const val VERSION = "0.4.1"
    const val DEPS = "required-after:saomclib@[1.2,)"
    val LOGGER: Logger = LogManager.getLogger(MODID)

    @SidedProxy(clientSide = "com.tencao.nmd.core.proxies.ClientProxy", serverSide = "com.tencao.nmd.core.proxies.ServerProxy", modId = MODID)
    lateinit var proxy: IProxy

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        // ========== Register Data Storage ==========
        CapabilitiesHandler.registerEntityCapability(PlayerData::class.java, PlayerData.Storage()) { `object`: Any -> PlayerHelper.isPlayer(`object`) }

        // ========== Register Keybinds ==========
        proxy.registerKeyBinds()

        // ========== Register Events ==========
        proxy.registerEvents()

        // ========== Register Network Packets ==========
        PacketPipeline.registerMessage(BlackListPKT::class.java, BlackListPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(SoundPKT::class.java, SoundPKT.Companion.Handler::class.java)

        // ========== Register GUI ==========
        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler)

    }


    @Mod.EventHandler
    fun serverStarting(event: FMLServerStartingEvent) {
        event.registerServerCommand(NMDCommand)
    }

}