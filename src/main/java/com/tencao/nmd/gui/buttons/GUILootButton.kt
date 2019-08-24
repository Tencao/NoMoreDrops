package com.tencao.nmd.gui.buttons

import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.api.IRarity
import net.minecraft.client.gui.GuiButton

class GUILootButton(val rarity: IRarity, var lootSetting: ILootSettings, buttonId: Int, x: Int, y: Int, widthIn: Int, heightIn: Int): GuiButton(buttonId, x, y, widthIn, heightIn, "${rarity.displayName}: ${lootSetting.displayName}") {

    fun updateLootSetting(lootSetting: ILootSettings){
        this.lootSetting = lootSetting
        this.displayString = "${rarity.displayName}: ${lootSetting.displayName}"
    }

}