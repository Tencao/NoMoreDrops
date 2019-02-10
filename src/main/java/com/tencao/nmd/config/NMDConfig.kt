package com.tencao.nmd.config

import com.tencao.nmd.core.NMDCore
import net.minecraftforge.common.config.Config

@Config(modid = NMDCore.MODID, name = NMDCore.NAME + "/config")
object NMDConfig {

    @Config.Comment("If the looting module should be enabled")
    var lootModule = true

    @Config.Comment("If the party module should be enabled")
    var partyModule = true

    @Config.Comment("Loot settings for the various systems")
    var lootcfg = LootConfig

    @Config.Comment("Loot settings for the various systems")
    var partycfg = PartyConfig

    object LootConfig {
        @Config.RangeInt(min = 0, max = 100)
        @Config.Comment("Minimum distance needed to automatically pickup the loot")
        var distanceForDrop = 4
    }

    object PartyConfig {
        @Config.RangeInt(min = 0, max = 10000)
        @Config.Comment("The time in seconds for each loot roll.")
        var LootRollTimer = 200

        @Config.RangeInt(min = 0, max = 100)
        @Config.Comment("Minimum drop chance for items to be considered for need or greed")
        var minimumDropChance = 20
    }
}

