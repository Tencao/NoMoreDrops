package com.tencao.nmd.network

import com.tencao.nmd.NMDCore
import com.tencao.nmd.network.packets.BlackListPKT
import com.tencao.nmd.network.packets.SoundPKT
import com.tencao.nmd.util.PlayerHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.relauncher.Side

object PacketHandler {

    private val HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel(NMDCore.MODID)

    fun register() {
        var disc = 0
        HANDLER.registerMessage(BlackListPKT.Handler::class.java, BlackListPKT::class.java, disc++, Side.CLIENT)
        HANDLER.registerMessage(BlackListPKT.Handler::class.java, BlackListPKT::class.java, disc++, Side.SERVER)
        HANDLER.registerMessage(SoundPKT.Handler::class.java, SoundPKT::class.java, disc++, Side.CLIENT)
    }

    /**
     * Sends a packet to the server.<br></br>
     * Must be called Client side.
     */
    fun sendToServer(msg: IMessage) {
        HANDLER.sendToServer(msg)
    }

    /**
     * Sends a packet to all the clients.<br></br>
     * Must be called Server side.
     */
    fun sendToAll(msg: IMessage) {
        HANDLER.sendToAll(msg)
    }

    /**
     * Send a packet to all players around a specific point.<br></br>
     * Must be called Server side.
     */
    fun sendToAllAround(msg: IMessage, point: NetworkRegistry.TargetPoint) {
        HANDLER.sendToAllAround(msg, point)
    }

    /**
     * Send a packet to a specific player.<br></br>
     * Must be called Server side.
     */
    fun sendTo(msg: IMessage, player: EntityPlayer) {
        if (PlayerHelper.isPlayer(player)) {
            HANDLER.sendTo(msg, player as EntityPlayerMP)
        }
    }

    /**
     * Send a packet to all the players in the specified dimension.<br></br>
     * Must be called Server side.
     */
    fun sendToDimension(msg: IMessage, dimension: Int) {
        HANDLER.sendToDimension(msg, dimension)
    }
}