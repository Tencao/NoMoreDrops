package com.tencao.nmd.config;

import com.tencao.nmd.NMDCore;
import net.minecraftforge.common.config.Config;

@Config(modid = NMDCore.MODID, name = NMDCore.NAME + "/config")
public final class NMDConfig {

    @Config.Comment({"Loot settings for the various systems"})
    public static final Loot loot = new Loot();
    public static class Loot {

        @Config.RangeInt(min = 0, max = 10000)
        @Config.Comment("The time in seconds for each loot roll.")
        public int LootRollTimer = 200;

        @Config.RangeInt(min = 0, max = 100)
        @Config.Comment("Minimum drop chance for items to be considered for need or greed")
        public int minimumDropChance = 20;

    }
}
