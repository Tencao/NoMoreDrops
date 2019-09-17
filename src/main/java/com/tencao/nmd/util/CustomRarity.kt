package com.tencao.nmd.util

import com.tencao.nmd.api.IRarity
import net.minecraft.util.text.TextFormatting

class CustomRarity(override val rgb: Int, override val hasEffect: Boolean = false): IRarity {

    override val displayName: String = "Unknown"

    constructor(color: TextFormatting?, hasEffect: Boolean): this(ColorUtil.getColor(color), hasEffect)
}