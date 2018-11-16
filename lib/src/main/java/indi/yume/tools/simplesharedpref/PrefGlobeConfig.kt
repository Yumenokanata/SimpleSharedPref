package indi.yume.tools.simplesharedpref

import android.annotation.SuppressLint
import android.content.Context
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
object PrefGlobeConfig {
    lateinit var context: Context

    var showLog: Boolean = BuildConfig.DEBUG

    /**
     * Is SharedPreferences.Editor write field default by commit()
     *
     * false: apply()
     * true:  commit()
     */
    var defaultWriteIsCommit: Boolean = false

    fun init(context: Context) {
        this.context = context
    }
}