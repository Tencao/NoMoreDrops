package com.tencao.nmd.api

interface IRarity{

    val rgb: Int

    val displayName: String

    val hasEffect: Boolean
        get() = false
}