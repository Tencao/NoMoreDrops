package com.tencao.nmd.events.handler

import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.data.SimpleEntityItem
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event
import java.util.*

/**
 * This is the event that fires before party looting is assigned
 */

@Cancelable
class PartyLootEvent(val entityItem: SimpleEntityItem, val party: IParty, var lootSetting: ILootSettings, val dropRarity: IRarity, val rollID: UUID = UUID.randomUUID()): Event()

