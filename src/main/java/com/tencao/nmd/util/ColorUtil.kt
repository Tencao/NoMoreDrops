package com.tencao.nmd.util

import net.minecraft.util.text.TextFormatting

enum class ColorUtil(val rgb: Int) {
    BLACK(0x000000),
    DARK_BLUE(0x0000AA),
    DARK_GREEN(0X00AA00),
    DARK_AQUA(0X00AAAA),
    DARK_RED(0XAA0000),
    DARK_PURPLE(0XAA00AA),
    GOLD(0XFFAA00),
    GRAY(0XAAAAAA),
    DARK_GRAY(0X555555),
    BLUE(0X5555FF),
    GREEN(0X55FF55),
    AQUA(0X55FFFF),
    RED(0XFF5555),
    LIGHT_PURPLE(0XFF55FF),
    YELLOW(0XFFFF55),
    WHITE(0XFFFFFF);

    companion object{
        fun getColor(color: TextFormatting?): Int{
            return when {
                color == null -> WHITE.rgb
                color.isColor -> values()[color.colorIndex].rgb
                else -> WHITE.rgb
            }
        }
    }
}