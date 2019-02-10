package com.tencao.nmd.loot.events.handler

import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.party.data.SimpleEntityItem
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * This is the event that's called both any loot is processed
 * @param entityItem = The item being processed
 * @param dropRarity = The rarity being set
 * @param isDrop = If true, the loot will be dropped, else it will be sent to PartyLootEvent.
 */

@Cancelable
class LootDropEvent(val entityItem: SimpleEntityItem, val party: IParty, var dropRarity: IRarity, val isDrop: Boolean): Event()