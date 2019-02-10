package com.tencao.nmd.core.util

import com.google.common.collect.ImmutableBiMap
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.input.Keyboard

@SideOnly(Side.CLIENT)
object ClientKeyHelper {

    lateinit var mcToNmd: ImmutableBiMap<KeyBinding, Keybinds>

    fun registerMCBindings() {
        val builder = ImmutableBiMap.builder<KeyBinding, Keybinds>()
        val var1 = Keybinds.values()
        val var2 = var1.size

        for (var3 in 0 until var2) {
            val k = var1[var3]
            val mcK = KeyBinding(k.keyName, k.defaultKeyCode, "NMD")
            builder.put(mcK, k)
            ClientRegistry.registerKeyBinding(mcK)
        }

        mcToNmd = builder.build()
    }

    fun getKeyName(k: Keybinds): String {
        val keyCode = (mcToNmd.inverse()[k] as KeyBinding).keyCode
        return if (keyCode <= Keyboard.getKeyCount() && keyCode >= 0) Keyboard.getKeyName(keyCode) else "INVALID KEY"
    }

    fun getKeyName(k: KeyBinding): String {
        val keyCode = k.keyCode
        return if (keyCode <= Keyboard.getKeyCount() && keyCode >= 0) Keyboard.getKeyName(keyCode) else "INVALID KEY"
    }
}