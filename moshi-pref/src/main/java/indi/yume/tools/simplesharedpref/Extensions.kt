package indi.yume.tools.simplesharedpref

import com.squareup.moshi.Moshi
import indi.yume.tools.simplesharedpref.extensions.createPipe
import indi.yume.tools.simplesharedpref.extensions.nonNull
import indi.yume.tools.simplesharedpref.extensions.pipeNullable
import indi.yume.tools.simplesharedpref.extensions.stringNullablePref
import java.lang.reflect.Type


private var defaultMoshi: Moshi? = null

var PrefGlobeConfig.moshi: Moshi?
    get() = defaultMoshi
    set(value) {
        defaultMoshi = value
    }

val moshiNullError = IllegalStateException("Moshi has not been set to Kotpref")

fun <T : Any> byMoshi(clazz: Class<T>, moshi: Moshi = defaultMoshi ?: throw moshiNullError): Pipe<String, T> {
    val adapter = moshi.adapter(clazz)
    return createPipe(write = { r -> adapter.toJson(r) },
        read = { w -> adapter.fromJson(w)!! })
}

fun <T : Any> byMoshi(typeOfT: Type, moshi: Moshi = defaultMoshi ?: throw moshiNullError): Pipe<String, T> {
    val adapter = moshi.adapter<T>(typeOfT)
    return createPipe(write = { r -> adapter.toJson(r) },
        read = { w -> adapter.fromJson(w)!! })
}

inline fun <reified T : Any> byMoshi(moshi: Moshi = PrefGlobeConfig.moshi ?: throw moshiNullError): Pipe<String, T> =
    byMoshi(T::class.java, moshi)


inline fun <reified T : Any> moshiNullablePref(moshi: Moshi = PrefGlobeConfig.moshi ?: throw moshiNullError) : SharedField<T?> =
    stringNullablePref.pipeNullable(byMoshi(moshi))

fun <T : Any> moshiNullablePref(clazz: Class<T>, moshi: Moshi = PrefGlobeConfig.moshi ?: throw moshiNullError) : SharedField<T?> =
    stringNullablePref.pipeNullable(byMoshi(clazz, moshi))

fun <T : Any> moshiNullablePref(typeOfT: Type, moshi: Moshi = PrefGlobeConfig.moshi ?: throw moshiNullError) : SharedField<T?> =
    stringNullablePref.pipeNullable(byMoshi(typeOfT, moshi))


inline fun <reified T : Any> moshiPref(defValue: T, moshi: Moshi = PrefGlobeConfig.moshi ?: throw moshiNullError) : SharedField<T> =
    moshiNullablePref<T>(moshi).pipe(nonNull(defValue))

fun <T : Any> moshiPref(clazz: Class<T>, defValue: T, moshi: Moshi = PrefGlobeConfig.moshi ?: throw moshiNullError) : SharedField<T> =
    moshiNullablePref(clazz, moshi).pipe(nonNull(defValue))

fun <T : Any> moshiPref(typeOfT: Type, defValue: T, moshi: Moshi = PrefGlobeConfig.moshi ?: throw moshiNullError) : SharedField<T> =
    moshiNullablePref<T>(typeOfT, moshi).pipe(nonNull(defValue))

