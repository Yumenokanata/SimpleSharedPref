package indi.yume.tools.simplesharedpref

import android.content.Context

typealias ContextProvider = () -> Context

val staticContextProvider: ContextProvider = { PrefGlobeConfig.context }
