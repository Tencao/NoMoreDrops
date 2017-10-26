package com.tencao.nmd

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import com.tencao.nmd.capability.PlayerData
import com.tencao.nmd.events.BlockEventHandler
import com.tencao.nmd.events.LivingEventHandler
import com.tencao.nmd.network.commands.NMDCommand
import com.tencao.nmd.util.PlayerHelper
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(modid = NMDCore.MODID, name = "No More Drops", version = NMDCore.VERSION, dependencies = NMDCore.DEPS)
object NMDCore {
    const val MODID = "nmd"
    const val VERSION = "0.1.2"
    const val DEPS = "required-after:saomclib@[1.2,)"
    val LOGGER: Logger = LogManager.getLogger(MODID)

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        LOGGER.info("Setting up data class")
        CapabilitiesHandler.registerEntityCapability(PlayerData::class.java, PlayerData.Storage()) { `object`: Any -> PlayerHelper.isPlayer(`object`) }


        LOGGER.info("Setting up events")
        MinecraftForge.EVENT_BUS.register(BlockEventHandler())
        MinecraftForge.EVENT_BUS.register(LivingEventHandler())
    }

    @Mod.EventHandler
    fun serverStarting(event: FMLServerStartingEvent) {
        event.registerServerCommand(NMDCommand)
    }

}