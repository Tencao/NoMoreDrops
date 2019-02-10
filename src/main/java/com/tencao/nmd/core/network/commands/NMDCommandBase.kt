package com.tencao.nmd.core.network.commands

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextFormatting

abstract class NMDCommandBase : CommandBase() {

    abstract override fun getName(): String

    abstract override fun getRequiredPermissionLevel(): Int

    abstract override fun getUsage(sender: ICommandSender): String

    @Throws(CommandException::class)
    abstract override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>)

    protected fun sendSuccess(sender: ICommandSender, message: ITextComponent) {
        sendMessage(sender, message.setStyle(Style().setParentStyle(message.style).setColor(TextFormatting.GREEN)))
    }

    protected fun sendError(sender: ICommandSender, message: ITextComponent) {
        sendMessage(sender, message.setStyle(Style().setParentStyle(message.style).setColor(TextFormatting.RED)))
    }

    protected fun sendMessage(sender: ICommandSender, message: ITextComponent) {
        sender.sendMessage(message)
    }
}
