package com.tencao.nmd.network.commands

import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextFormatting

interface NMDCommandBase {

    /**
     * Gets the name of the command
     */
    fun getID(): String

    fun getRequiredPermissionLevel(): Int {
        return 0
    }

    fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): List<String> {
        return emptyList()
    }

    /**
     * Check if the given ICommandSender has permission to execute this command
     */
    fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
        return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getID())
    }

    fun getUsage(sender: ICommandSender): String

    @Throws(CommandException::class)
    fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>)

    fun sendSuccess(sender: ICommandSender, message: ITextComponent) {
        sendMessage(sender, message.setStyle(Style().setParentStyle(message.style).setColor(TextFormatting.GREEN)))
    }

    fun sendError(sender: ICommandSender, message: ITextComponent) {
        sendMessage(sender, message.setStyle(Style().setParentStyle(message.style).setColor(TextFormatting.RED)))
    }

    fun sendMessage(sender: ICommandSender, message: ITextComponent) {
        sender.sendMessage(message)
    }
}
