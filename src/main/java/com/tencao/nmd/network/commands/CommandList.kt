package com.tencao.nmd.network.commands

import be.bluexin.saomclib.capabilities.getPartyCapability
import com.tencao.nmd.DropRarityEnum
import com.tencao.nmd.LootSettingsEnum
import com.tencao.nmd.capability.getNMDData
import com.tencao.nmd.util.LowerCasePrefixPredicate
import com.tencao.nmd.util.PlayerHelper
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import java.util.*

enum class CommandList: NMDCommandBase {
    BLACK_LIST {
        override fun execute(server: MinecraftServer, sender: ICommandSender, params: Array<String>) {
            if (PlayerHelper.isPlayer(sender.commandSenderEntity)) {
                val player: EntityPlayer = (sender.commandSenderEntity as EntityPlayer)
                if (player.heldItemMainhand != ItemStack.EMPTY) {
                    if (player.getNMDData().modifyBlackList(player.heldItemMainhand))
                        sendSuccess(sender, TextComponentTranslation("nmd.blacklist.modified", player.heldItemMainhand.displayName))
                }
                else
                    sendError(sender, TextComponentTranslation("nmd.blacklist.null"))
            }
        }

    },
    LOOT_SETTINGS {
        override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, params: Array<String>, pos: BlockPos?): List<String> {
            if (params.size == 1) {
                return DropRarityEnum.values().map { it.name }.filter (LowerCasePrefixPredicate(params[0]))
            }
            if (params.size == 2) {
                return LootSettingsEnum.values().map { it.name }.filter (LowerCasePrefixPredicate(params[0]))
            }
            return emptyList()
        }

        override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
            val player = if (PlayerHelper.isPlayer(sender.commandSenderEntity)) sender.commandSenderEntity as EntityPlayer else return false
            return player.getPartyCapability().partyData == null || player.getPartyCapability().partyData?.isLeader(player) == true
        }

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

    };

    override fun getID(): String {
        return TextComponentTranslation("nmd.command.${name.toLowerCase()}").unformattedText
    }

    override fun getUsage(sender: ICommandSender): String {
        return "nmd.command.${name.toLowerCase()}.usage"
    }
}