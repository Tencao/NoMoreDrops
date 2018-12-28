package com.tencao.nmd.network.commands

import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.util.PlayerHelper
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation

object BlackListCMD : NMDCommandBase() {

    override fun getName(): String {
        return "nmd_blacklist"
    }

    override fun getUsage(sender: ICommandSender): String {
        return "nmd.command.blacklist.usage"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
        return true
    }

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
        if (PlayerHelper.isPlayer(sender.commandSenderEntity)) {
            val player: EntityPlayer = (sender.commandSenderEntity as EntityPlayer)
            if (player.heldItemMainhand != ItemStack.EMPTY) {
                if (player.getNMDData().modifyBlackList(player.heldItemMainhand))
                    sendSuccess(sender, TextComponentTranslation("nmd.blacklist.modified", player.heldItemMainhand.displayName))
            }
            else
                sendError(sender, TextComponentTranslation("nmd.blacklist.null"))

            //PacketHandler.sendTo(BlackListPKT((sender.commandSenderEntity as EntityPlayer).getNMDData().getItemList()), (sender.commandSenderEntity as EntityPlayer))
            //(sender.commandSenderEntity as EntityPlayer).openGui(this, 0, sender.entityWorld, sender.position.x, sender.position.y, sender.position.z)
        }
    }
}