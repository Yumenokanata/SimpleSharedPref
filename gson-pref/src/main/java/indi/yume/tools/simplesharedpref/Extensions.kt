package indi.yume.tools.simplesharedpref

import com.google.gson.Gson
import indi.yume.tools.simplesharedpref.extensions.createPipe
import indi.yume.tools.simplesharedpref.extensions.nonNull
import indi.yume.tools.simplesharedpref.extensions.pipeNullable
import indi.yume.tools.simplesharedpref.extensions.stringNullablePref
import java.lang.reflect.Type


private var defaultGson: Gson? = null

var PrefGlobeConfig.gson: Gson?
    get() = defaultGson
    set(value) {
        defaultGson = value
    }

val gsonNullError = IllegalStateException("Gson has not been set to Kotpref")

fun <T : Any> byGson(clazz: Class<T>, gson: Gson = defaultGson ?: throw gsonNullError): Pipe<String, T> =
    createPipe(write = { r -> gson.toJson(r) }, read = { w -> gson.fromJson(w, clazz)!! })

fun <T : Any> byGson(typeOfT: Type, gson: Gson = defaultGson ?: throw gsonNullError): Pipe<String, T> =
    createPipe(write = { r -> gson.toJson(r) }, read = { w -> gson.fromJson(w, typeOfT)!! })

inline fun <reified T : Any> byGson(gson: Gson = PrefGlobeConfig.gson ?: throw gsonNullError): Pipe<String, T> =
    byGson(T::class.java, gson)


inline fun <reified T : Any> gsonNullablePref(gson: Gson = PrefGlobeConfig.gson ?: throw gsonNullError) : SharedField<T?> =
    stringNullablePref.pipeNullable(byGson(gson))

fun <T : Any> gsonNullablePref(clazz: Class<T>, gson: Gson = PrefGlobeConfig.gson ?: throw gsonNullError) : SharedField<T?> =
    stringNullablePref.pipeNullable(byGson(clazz, gson))

fun <T : Any> gsonNullablePref(typeOfT: Type, gson: Gson = PrefGlobeConfig.gson ?: throw gsonNullError) : SharedField<T?> =
    stringNullablePref.pipeNullable(byGson(typeOfT, gson))


inline fun <reified T : Any> gsonPref(defValue: T, gson: Gson = PrefGlobeConfig.gson ?: throw gsonNullError) : SharedField<T> =
    gsonNullablePref<T>(gson).pipe(nonNull(defValue))

fun <T : Any> gsonPref(clazz: Class<T>, defValue: T, gson: Gson = PrefGlobeConfig.gson ?: throw gsonNullError) : SharedField<T> =
    gsonNullablePref(clazz, gson).pipe(nonNull(defValue))

fun <T : Any> gsonPref(typeOfT: Type, defValue: T, gson: Gson = PrefGlobeConfig.gson ?: throw gsonNullError) : SharedField<T> =
    gsonNullablePref<T>(typeOfT, gson).pipe(nonNull(defValue))