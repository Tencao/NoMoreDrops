package com.tencao.nmd.core.util

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.profiler.Profiler
import net.minecraft.util.EnumFacing
import net.minecraft.world.*
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.chunk.storage.IChunkLoader
import net.minecraft.world.gen.structure.template.TemplateManager
import net.minecraft.world.storage.IPlayerFileData
import net.minecraft.world.storage.ISaveHandler
import net.minecraft.world.storage.WorldInfo
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityDispatcher
import net.minecraftforge.event.ForgeEventFactory
import java.io.File
import java.io.IOException

class FakeWorld: World(saveHandler, worldInfo, worldProvider, Profiler(), false) {

    private val capabilities: CapabilityDispatcher? = ForgeEventFactory.gatherCapabilities(this, null)

    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        return capabilities?.getCapability(capability, facing)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
        return capabilities != null && capabilities.hasCapability(capability, facing)
    }

    override fun createChunkProvider(): IChunkProvider {
        return object : IChunkProvider {
            override fun getLoadedChunk(x: Int, z: Int): Chunk? {
                return null
            }

            override fun provideChunk(x: Int, z: Int): Chunk? {
                return null
            }

            override fun tick(): Boolean {
                return false
            }

            override fun makeString(): String? {
                return null
            }

            override fun isChunkGeneratedAt(p_191062_1_: Int, p_191062_2_: Int): Boolean {
                return false
            }
        }
    }

    override fun isChunkLoaded(x: Int, z: Int, allowEmpty: Boolean): Boolean {
        return false
    }

    companion object {
        private val worldSettings = WorldSettings(0, GameType.SURVIVAL, true, false, WorldType.DEFAULT)
        private val worldInfo = WorldInfo(worldSettings, "nmd_fake")
        val saveHandler = FakeSaveHandler()
        val fakeWorld = FakeWorld()

        val worldProvider: WorldProvider = object : WorldProvider() {
            override fun getDimensionType(): DimensionType {
                return DimensionType.OVERWORLD
            }

            override fun getWorldTime(): Long {
                return worldInfo.worldTime
            }
        }
    }


    class FakeSaveHandler : ISaveHandler {

        override fun loadWorldInfo(): WorldInfo? {
            return worldInfo
        }

        @Throws(MinecraftException::class)
        override fun checkSessionLock() {

        }

        override fun getChunkLoader(provider: WorldProvider): IChunkLoader {
            return object : IChunkLoader {
                @Throws(IOException::class)
                override fun loadChunk(worldIn: World, x: Int, z: Int): Chunk? {
                    return null
                }

                @Throws(MinecraftException::class, IOException::class)
                override fun saveChunk(worldIn: World, chunkIn: Chunk) {

                }

                @Throws(IOException::class)
                override fun saveExtraChunkData(worldIn: World, chunkIn: Chunk) {

                }

                override fun chunkTick() {

                }

                override fun flush() {

                }

                override fun isChunkGeneratedAt(p_191063_1_: Int, p_191063_2_: Int): Boolean {
                    return false
                }
            }
        }

        override fun saveWorldInfoWithPlayer(worldInformation: WorldInfo, tagCompound: NBTTagCompound) {

        }

        override fun saveWorldInfo(worldInformation: WorldInfo) {

        }

        override fun getPlayerNBTManager(): IPlayerFileData {
            return object : IPlayerFileData {
                override fun writePlayerData(player: EntityPlayer) {

                }

                override fun readPlayerData(player: EntityPlayer): NBTTagCompound? {
                    return NBTTagCompound()
                }

                override fun getAvailablePlayerDat(): Array<String?> {
                    return arrayOfNulls(0)
                }
            }
        }

        override fun flush() {

        }

        override fun getWorldDirectory(): File? {
            return null
        }

        override fun getMapFileFromName(mapName: String): File? {
            return null
        }

        override fun getStructureTemplateManager(): TemplateManager? {
            return null
        }
    }
}