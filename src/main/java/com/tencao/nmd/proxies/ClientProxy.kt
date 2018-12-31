package com.tencao.nmd.proxies

import com.tencao.nmd.util.ClientKeyHelper

object ClientProxy: IProxy {

    override fun registerKeyBinds() {
        ClientKeyHelper.registerMCBindings()
    }
}