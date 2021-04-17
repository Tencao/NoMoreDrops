package com.tencao.nmd.data

import com.tencao.nmd.DropRarityEnum
import com.tencao.nmd.api.IRarity

data class LootObject(val stack: SimpleEntityItem, val rarity: IRarity = DropRarityEnum.COMMON)
