package indi.yume.tools.simplesharedpref

import android.content.Context
import android.content.SharedPreferences

abstract class PrefModel(val contextProvider: () -> Context = staticContextProvider) {
    open val fileName: String = javaClass.simpleName

    open val prefMode: Int = Context.MODE_PRIVATE

    val context: Context
        get() = contextProvider()

    val sharedPreference: SharedPreferences by lazy {
        context.getSharedPreferences(fileName, prefMode)
    }

    fun <T> SharedField<T>.bind(key: PrefKey? = null, commitWrite: Boolean = PrefGlobeConfig.defaultWriteIsCommit)
            : Binder<T> = bindTo(this@PrefModel, key, commitWrite)
}
