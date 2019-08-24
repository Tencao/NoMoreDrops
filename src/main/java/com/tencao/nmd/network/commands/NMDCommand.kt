package com.tencao.nmd.network.commands

import com.tencao.nmd.util.LowerCasePrefixPredicate
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import java.util.*

object NMDCommand : CommandBase() {

    private val commands = CommandList.values()

    override fun getName(): String {
        return "nmd"
    }

    override fun getUsage(sender: ICommandSender): String {
        return "nmd.command.main.usage"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
        return true
    }

    override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): List<String> {
        if (params.size <= 1) {
            return commands.map { it.getID().toLowerCase() }.filter (LowerCasePrefixPredicate(params[0]))
        }
        else return commands.asSequence().firstOrNull { it.getID().equals(params[0], true) }?.getTabCompletions(server, sender, params, pos)?: return emptyList()
    }

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
        if (params.isEmpty()) {
            sendError(sender, TextComponentTranslation("nmd.command.main.usage"))
            return
        }

        if (params.isNotEmpty()) {
            val subName = params[0].toLowerCase(Locale.ROOT)

            commands.firstOrNull { command -> command.getID().equals(subName, true) }?.let {command ->
                if (command.checkPermission(server, sender))
                    command.execute(server, sender, params.drop(1).toTypedArray())
                else
                    sendError(sender, TextComponentTranslation("commands.generic.permission"))
            }?: sendError(sender, TextComponentTranslation("pixelspawn.command.main.usage"))

        }
    }

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