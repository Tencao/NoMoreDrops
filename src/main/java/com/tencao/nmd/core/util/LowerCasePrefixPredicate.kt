package com.tencao.nmd.core.util

import java.util.*

class LowerCasePrefixPredicate(private val prefix: String) : (String?) -> Boolean {
    override fun invoke(p1: String?): Boolean {return p1?.toLowerCase(Locale.ROOT)!!.startsWith(prefix.toLowerCase(Locale.ROOT)) }
}