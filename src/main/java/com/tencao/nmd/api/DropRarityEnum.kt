package com.tencao.nmd.api

/**
 * This defines the rarity given to the item based on the drop rate, however this is changeable in the lootdrop event
 */
enum class DropRarityEnum: IRarity {
    UNKNOWN{
        override val rgb: Int = 0x828282
        override val displayName: String = "Unknown"
    },
    COMMON{
        override val rgb: Int = 0xffffff
        override val displayName: String = "Common"
    },
    UNCOMMON{
        override val rgb: Int = 0x19c427
        override val displayName: String = "Unknown"
    },
    RARE{
        override val rgb: Int = 0x3c47e8
        override val displayName: String = "Rare"
    },
    EPIC{
        override val rgb: Int = 0xa93ce8
        override val displayName: String = "Epic"
    };

}