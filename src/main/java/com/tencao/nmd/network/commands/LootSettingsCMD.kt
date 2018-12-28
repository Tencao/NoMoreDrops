package com.tencao.nmd.network.commands

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.packets.PacketPipeline
import com.google.common.collect.ImmutableList
import com.tencao.nmd.api.DropRarityEnum
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.api.LootSettingsEnum
import com.tencao.nmd.network.packets.LootSettingPKT
import com.tencao.nmd.util.LowerCasePrefixPredicate
import com.tencao.nmd.util.PlayerHelper
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
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
        return player.getPartyCapability().party?.isLeader(player) == true
    }

    override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): List<String> {
        if (params.size == 1) {
            return DropRarityEnum.values().map { it.name }.filter ( LowerCasePrefixPredicate(params[0]) )
        }
        if (params.size == 2) {
            return LootSettingsEnum.values().map { it.name }.filter ( LowerCasePrefixPredicate(params[0]) )
        }
        return ImmutableList.of<String>()
    }

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
        if (params.size < 2) {
            sendMessage(sender, TextComponentTranslation("nmd.lootsetting.current", (sender as EntityPlayer).getNMDData().lootSettings.toString()))
        }

        try {
            val rarity = DropRarityEnum.valueOf(params[0].toLowerCase(Locale.ROOT))
            val lootSettings = LootSettingsEnum.valueOf(params[1].toLowerCase(Locale.ROOT))
            (sender as EntityPlayer).getNMDData().setLootSetting(lootSettings, rarity)
            (sender).getPartyCapability().party?.members?.forEach {
                PacketPipeline.sendTo(LootSettingPKT(rarity, lootSettings), it as EntityPlayerMP)
            }
        } catch (e: Exception){
            sendError(sender, TextComponentTranslation("nmd.lootsetting.null"))
        }

    }
}