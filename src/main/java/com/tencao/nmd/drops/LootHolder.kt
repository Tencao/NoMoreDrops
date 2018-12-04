package com.tencao.nmd.drops

import net.minecraft.item.ItemStack
import java.util.*

object LootHolder {

    val needOrGreed = mutableListOf<Pair<ItemStack, Set<UUID>>>()
    val masterLooter = mutableListOf<Pair<ItemStack, Set<UUID>>>()
}