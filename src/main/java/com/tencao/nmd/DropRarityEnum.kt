package com.tencao.nmd

import com.tencao.nmd.api.IRarity

/**
 * This defines the rarity given to the item based on the drop rate, however this is changeable in the lootdrop event
 */
enum class DropRarityEnum: IRarity {
    UNKNOWN{
        override val rgb: Int = 0x828282
        override val displayName: String = "Unknown"
        override val hasEffect: Boolean
            get() = true
    },
    COMMON{
        override val rgb: Int = 0xffffff
        override val displayName: String = "Common"
        override val hasEffect: Boolean
            get() = true
    },
    UNCOMMON{
        override val rgb: Int = 0x55FF55
        override val displayName: String = "Unknown"
    },
    RARE{
        override val rgb: Int = 0x55FFFF
        override val displayName: String = "Rare"
    },
    EPIC{
        override val rgb: Int = 0xFF55FF
        override val displayName: String = "Epic"
    },
    LEGENDARY{
        override val rgb: Int = 0xFFFF55
        override val displayName: String = "Legendary"
        override val hasEffect: Boolean
            get() = true
    },
    MYTHIC{
        override val rgb: Int = 0xFF5555
        override val displayName: String = "Mythic"
        override val hasEffect: Boolean
            get() = true
    },
    GODLIKE{
        override val rgb: Int = 0xE6E6E6
        override val displayName: String = "Godlike"
        override val hasEffect: Boolean
            get() = true
    };

}