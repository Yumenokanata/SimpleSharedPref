package indi.yume.tools.simplesharedpref

import android.app.Application
import com.google.gson.Gson
import com.squareup.moshi.Moshi

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        PrefGlobeConfig.apply {
            moshi = Moshi.Builder().build()
//            gson = Gson()

            showLog = BuildConfig.DEBUG

            context = this@MainApplication
        }
    }
}