package com.tencao.nmd.api

/**
 * This defines the rarity given to the item based on the drop rate, however this is changeable in the lootdrop event
 */
enum class DropRarityEnum: IRarity {
    UNKNOWN{ override val rgb: Int = 0x828282 },
    COMMON{ override val rgb: Int = 0xffffff },
    UNCOMMON{ override val rgb: Int = 0x19c427 },
    RARE{ override val rgb: Int = 0x3c47e8 },
    EPIC{ override val rgb: Int = 0xa93ce8 };

}