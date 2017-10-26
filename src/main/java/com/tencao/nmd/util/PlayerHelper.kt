package com.tencao.nmd.util

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.util.FakePlayer

object PlayerHelper {

    /**
     * This makes sure the player is actually a player and not a fake player
     * @param object The object to check
     * *
     * @return Returns true if player
     */
    fun isPlayer(`object`: Any?): Boolean {
        return `object` is EntityPlayer && `object` !is FakePlayer
    }
}