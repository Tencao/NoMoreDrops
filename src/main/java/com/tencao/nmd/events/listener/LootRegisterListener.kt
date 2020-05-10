package com.tencao.nmd.events.listener

import com.tencao.nmd.DropRarityEnum
import com.tencao.nmd.LootSettingsEnum
import com.tencao.nmd.SpecialLootSettingsEnum
import com.tencao.nmd.events.handler.RegisterLootRarityEvent
import com.tencao.nmd.registry.LootRegistry
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LootRegisterListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun registerLootRarity(event: RegisterLootRarityEvent){
        event.registerLootRarity(DropRarityEnum.UNKNOWN, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.COMMON, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.UNCOMMON, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.RARE, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.EPIC, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.LEGENDARY, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.MYTHIC, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootRarity(DropRarityEnum.GODLIKE, SpecialLootSettingsEnum.NeedOrGreed)
        event.registerLootSettings(LootSettingsEnum.Random)
        event.registerLootSettings(LootSettingsEnum.RoundRobin)

        LootRegistry.registerDefaults(event.getLootSettings(), event.getLootOptions(), event.getRarities())
        event.isCanceled = true
    }
}