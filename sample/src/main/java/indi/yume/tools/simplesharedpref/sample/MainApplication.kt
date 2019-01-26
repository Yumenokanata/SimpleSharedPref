package indi.yume.tools.simplesharedpref.sample

import android.app.Application
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import indi.yume.tools.simplesharedpref.BuildConfig
import indi.yume.tools.simplesharedpref.PrefGlobeConfig
import indi.yume.tools.simplesharedpref.gson.gson
import indi.yume.tools.simplesharedpref.moshi.moshi

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        PrefGlobeConfig.apply {
            moshi = Moshi.Builder().build()
            gson = Gson()

            showLog = BuildConfig.DEBUG

            context = this@MainApplication
        }
    }
}