package com.tencao.nmd.party.network.commands

import be.bluexin.saomclib.capabilities.getPartyCapability
import com.google.common.collect.ImmutableList
import com.tencao.nmd.party.DropRarityEnum
import com.tencao.nmd.party.LootSettingsEnum
import com.tencao.nmd.core.capability.getNMDData
import com.tencao.nmd.core.network.commands.NMDCommandBase
import com.tencao.nmd.core.util.LowerCasePrefixPredicate
import com.tencao.nmd.core.util.PlayerHelper
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import java.util.*

object LootSettingsCMD : NMDCommandBase() {

    override fun getName(): String {
        return "nmd.lootsetting"
    }

    override fun getUsage(sender: ICommandSender): String {
        return "nmd.command.lootsetting.usage"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
        val player = if (PlayerHelper.isPlayer(sender.commandSenderEntity)) sender.commandSenderEntity as EntityPlayer else return false
        return player.getPartyCapability().getOrCreatePT().isLeader(player)
    }

    override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): List<String> {
        if (params.size == 1) {
            return DropRarityEnum.values().map { it.name }.filter (LowerCasePrefixPredicate(params[0]))
        }
        if (params.size == 2) {
            return LootSettingsEnum.values().map { it.name }.filter (LowerCasePrefixPredicate(params[0]))
        }
        return ImmutableList.of()
    }

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
        if (params.size < 2) {
            sendMessage(sender, TextComponentTranslation("nmd.lootsetting.current", (sender as EntityPlayer).getNMDData().lootSettings.toString()))
        }

        try {
            val rarity = DropRarityEnum.valueOf(params[0].toLowerCase(Locale.ROOT))
            val lootSettings = LootSettingsEnum.valueOf(params[1].toLowerCase(Locale.ROOT))
            (sender as EntityPlayer).getNMDData().setLootSetting(rarity, lootSettings,true)
        } catch (e: Exception){
            sendError(sender, TextComponentTranslation("nmd.lootsetting.null"))
        }

    }
}