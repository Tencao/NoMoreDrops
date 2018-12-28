package com.tencao.nmd.network.commands

import com.google.common.collect.ImmutableList
import com.tencao.nmd.util.LowerCasePrefixPredicate
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import java.util.*

object NMDCommand : NMDCommandBase() {

    private val blackListCmd = BlackListCMD
    private val lootSettingCmd = LootSettingsCMD
    private val commands = listOf("blacklist, lootsetting")

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
        if (params.size == 1) {
            return commands.filter ( LowerCasePrefixPredicate(params[0]) )
        }
        return ImmutableList.of<String>()
    }

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
        if (params.isEmpty()) {
            sendError(sender, TextComponentTranslation("nmd.command.main.usage"))
            return
        }

        if (params.isNotEmpty()) {
            val relayparams = Arrays.copyOfRange(params, 1, params.size)

            val subName = params[0].toLowerCase(Locale.ROOT)

            when (subName){
                "blacklist" ->
                    if (blackListCmd.checkPermission(server, sender))
                        blackListCmd.execute(server, sender, relayparams)
                    else
                        sendError(sender, TextComponentTranslation("commands.generic.permission"))

                "lootsetting" ->
                    if (lootSettingCmd.checkPermission(server, sender))
                        lootSettingCmd.execute(server, sender, relayparams)
                    else
                        sendError(sender, TextComponentTranslation("commands.generic.permission"))
                else -> {
                    sendError(sender, TextComponentTranslation("nmd.command.main.usage"))
                    return
                }
            }
        }
    }
}