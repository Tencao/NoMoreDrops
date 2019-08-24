package com.tencao.nmd.config;

import com.tencao.nmd.NMDCore;
import net.minecraftforge.common.config.Config;

@Config(modid = NMDCore.MODID, name = NMDCore.NAME + "/config")
public final class NMDConfig {

    @Config.Comment("Loot settings for the various systems")
    public static final LootConfig lootcfg = new LootConfig();

    @Config.Comment("Party settings for the various systems")
    public static final PartyConfig partycfg = new PartyConfig();

    public static class LootConfig {
        @Config.Comment("If the looting module should be enabled")
        public boolean lootModule = true;

        @Config.RangeInt(min = 0, max = 100)
        @Config.Comment("Minimum distance needed to automatically pickup the loot")
        public int distanceForDrop =  4;

        @Config.Comment("If all loot dropped by mobs should drop on the ground instead of auto looted.")
        public boolean alwaysDropMobLoot = false;
    }

    public static class PartyConfig {
        @Config.Comment("If the party module should be enabled")
        public boolean partyModule = true;

        @Config.RangeInt(min = 0, max = 10000)
        @Config.Comment("The time in seconds for each loot roll.")
        public int LootRollTimer = 200;

        @Config.RangeInt(min = 0, max = 100)
        @Config.Comment("Minimum drop chance for items to be considered for need or greed")
        public int minimumDropChance = 20;

    }
}
