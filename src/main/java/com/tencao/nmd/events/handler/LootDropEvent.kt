package com.tencao.nmd.events.handler

import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.data.SimpleEntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * This is the event that's called both any loot is processed
 * @param entityItem = The item being processed
 * @param dropRarity = The rarity being set
 * @param isDrop = If true, the loot will be dropped, else it will be sent to PartyLootEvent.
 */

@Cancelable
class LootDropEvent(val entityItem: SimpleEntityItem, val player: EntityPlayer, var dropRarity: IRarity, var lootSettings: ILootSettings, val isDrop: Boolean): Event()