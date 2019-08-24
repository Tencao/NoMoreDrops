package com.tencao.nmd.util

import net.minecraftforge.fml.common.Loader

object ModHelper {

    val isSAOUILoaded
        get() = Loader.isModLoaded("saoui")

    val isCustomNPCLoaded
        get() = Loader.isModLoaded("customnpcs")
}