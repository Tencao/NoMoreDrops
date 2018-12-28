package com.tencao.nmd

import be.bluexin.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.saomclib.packets.PacketPipeline
import com.tencao.nmd.capability.PlayerData
import com.tencao.nmd.events.*
import com.tencao.nmd.gui.ItemRollGUI
import com.tencao.nmd.network.commands.NMDCommand
import com.tencao.nmd.network.packets.BlackListPKT
import com.tencao.nmd.network.packets.LootClientPKT
import com.tencao.nmd.network.packets.LootSettingPKT
import com.tencao.nmd.network.packets.SoundPKT
import com.tencao.nmd.util.PlayerHelper
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*

@Mod(modid = NMDCore.MODID, name = NMDCore.NAME, version = NMDCore.VERSION, dependencies = NMDCore.DEPS)
object NMDCore {
    const val MODID = "nmd"
    const val NAME = "No More Drops"
    const val VERSION = "0.3.0"
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
        MinecraftForge.EVENT_BUS.register(BlockEventListener)
        MinecraftForge.EVENT_BUS.register(LivingEventListener)
        MinecraftForge.EVENT_BUS.register(EntityItemEventListener)
        MinecraftForge.EVENT_BUS.register(ItemRollGUI)
        MinecraftForge.EVENT_BUS.register(WorldEventListener)
        MinecraftForge.EVENT_BUS.register(PlayerEventListener)
        MinecraftForge.EVENT_BUS.register(PartyLootEventListener)

        PacketPipeline.registerMessage(BlackListPKT::class.java, BlackListPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(LootClientPKT::class.java, LootClientPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(LootSettingPKT::class.java, LootSettingPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(LootSettingPKT::class.java, LootSettingPKT.Companion.Handler::class.java)
        PacketPipeline.registerMessage(SoundPKT::class.java, SoundPKT.Companion.Handler::class.java)
    }

    @Mod.EventHandler
    fun loadFinished(event: FMLLoadCompleteEvent){
        MinecraftForge.EVENT_BUS.post(RegisterLootRarity(LinkedList()))
    }

    @Mod.EventHandler
    fun serverStarting(event: FMLServerStartingEvent) {
        event.registerServerCommand(NMDCommand)
    }

}