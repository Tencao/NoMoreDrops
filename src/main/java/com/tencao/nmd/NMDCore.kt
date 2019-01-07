package com.tencao.nmd

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.capability.PlayerData
import com.tencao.nmd.events.RegisterLootRarity
import com.tencao.nmd.gui.GuiHandler
import com.tencao.nmd.network.commands.NMDCommand
import com.tencao.nmd.network.packets.*
import com.tencao.nmd.proxies.IProxy
import com.tencao.nmd.util.PlayerHelper
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(modid = NMDCore.MODID, name = NMDCore.NAME, version = NMDCore.VERSION, dependencies = NMDCore.DEPS)
object NMDCore {
    const val MODID = "nmd"
    const val NAME = "No More Drops"
    const val VERSION = "0.3.3"
    const val DEPS = "required-after:saomclib@[1.2,)"
    val LOGGER: Logger = LogManager.getLogger(MODID)

    @SidedProxy(clientSide = "com.tencao.nmd.proxies.ClientProxy", serverSide = "com.tencao.nmd.proxies.ServerProxy")
    lateinit var proxy: IProxy

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        CapabilitiesHandler.registerEntityCapability(PlayerData::class.java, PlayerData.Storage()) { `object`: Any -> PlayerHelper.isPlayer(`object`) }

        proxy.registerEvents()
        proxy.registerKeyBinds()

        PacketPipeline.registerMessage(BlackListPKT::class.java, BlackListPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(LootClientPKT::class.java, LootClientPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(LootServerPKT::class.java, LootServerPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(LootSettingPKT::class.java, LootSettingPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(SoundPKT::class.java, SoundPKT.Companion.Handler::class.java)

        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler)
    }

    @Mod.EventHandler
    fun loadFinished(event: FMLLoadCompleteEvent){
        MinecraftForge.EVENT_BUS.post(RegisterLootRarity())
    }

    @Mod.EventHandler
    fun serverStarting(event: FMLServerStartingEvent) {
        event.registerServerCommand(NMDCommand)
    }

}