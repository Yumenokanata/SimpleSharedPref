package indi.yume.tools.simplesharedpref

import android.annotation.SuppressLint
import android.content.SharedPreferences
import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.data.ReaderT
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.functor.functor
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class BaseType<T>(val baseField: SharedField<T>)

object SharedString : BaseType<String?>(
    baseField = SharedField(
        writer = ReaderT { (ed, k, r) -> IO { ed.putString(k, r); Unit } },
        reader = ReaderT { (sp, k) -> IO { sp.getString(k, null) } }
    )
)

object SharedStringSet : BaseType<Set<String>?>(
    baseField = SharedField(
        writer = ReaderT { (ed, k, r) -> IO { ed.putStringSet(k, r); Unit } },
        reader = ReaderT { (sp, k) -> IO { sp.getStringSet(k, null) } }
    )
)

object SharedInt : BaseType<Int?>(
    baseField = SharedField(
        writer = ReaderT { (ed, k, r) -> IO { if (r != null) ed.putInt(k, r) else ed.remove(k); Unit } },
        reader = ReaderT { (sp, k) -> IO { if (sp.contains(k)) sp.getInt(k, 0) else null } }
    )
)

object SharedLong : BaseType<Long?>(
    baseField = SharedField(
        writer = ReaderT { (ed, k, r) -> IO { if (r != null) ed.putLong(k, r) else ed.remove(k); Unit } },
        reader = ReaderT { (sp, k) -> IO { if (sp.contains(k)) sp.getLong(k, 0) else null } }
    )
)

object SharedFloat : BaseType<Float?>(
    baseField = SharedField(
        writer = ReaderT { (ed, k, r) -> IO { if (r != null) ed.putFloat(k, r) else ed.remove(k); Unit } },
        reader = ReaderT { (sp, k) -> IO { if (sp.contains(k)) sp.getFloat(k, 0f) else null } }
    )
)

object SharedBoolean : BaseType<Boolean?>(
    baseField = SharedField(
        writer = ReaderT { (ed, k, r) -> IO { if (r != null) ed.putBoolean(k, r) else ed.remove(k); Unit } },
        reader = ReaderT { (sp, k) -> IO { if (sp.contains(k)) sp.getBoolean(k, false) else null } }
    )
)


typealias PrefKey = String

data class SharedField<T>(
    val writer: ReaderT<ForIO, Tuple3<SharedPreferences.Editor, PrefKey, T>, Unit>,
    val reader: ReaderT<ForIO, Tuple2<SharedPreferences, PrefKey>, T>
) {
    fun <R> map(wf: (R) -> T, rf: (T) -> R): SharedField<R> =
            SharedField(ReaderT { (ed, k, r) -> writer.run(Tuple3(ed, k, wf(r))) },
                reader.map(IO.functor(), rf))

    fun bindTo(prefModel: PrefModel,
               key: PrefKey? = null,
               commitWrite: Boolean = PrefGlobeConfig.defaultWriteIsCommit): Binder<T> =
        Binder(key, this@SharedField, commitWrite, prefModel)

    infix fun <R> pipe(process: Pipe<T, R>): SharedField<R> =
        map(process::reverseGet, process::get)
}

class Binder<T>(
    val key: PrefKey?,
    val field: SharedField<T>,
    val commitWrite: Boolean = PrefGlobeConfig.defaultWriteIsCommit,
    val prefModel: PrefModel): ReadWriteProperty<Any?, T> {

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val result = field.reader.run(Tuple2(prefModel.sharedPreference, key ?: property.name))
            .fix().attempt().unsafeRunSync()

        return when (result) {
            is Either.Left<Throwable> -> throw result.a
            is Either.Right<T> -> result.b
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val editor = prefModel.sharedPreference.edit()
        val result = field.writer.run(Tuple3(editor, key ?: property.name, value))
            .fix().attempt().unsafeRunSync()

        when (result) {
            is Either.Left<Throwable> -> {
                if (PrefGlobeConfig.showLog)
                    result.a.printStackTrace()
            }
            is Either.Right<Unit> -> {
                if (commitWrite)
                    editor.commit()
                else
                    editor.apply()
            }
        }
    }
}

interface Pipe<A, B> {
    fun get(a: A): B

    fun reverseGet(b: B): A

    companion object {
        operator fun <A, B> invoke(get: (A) -> B, reverseGet: (B) -> A): Pipe<A, B> = object : Pipe<A, B> {
            override fun get(a: A): B = get(a)

            override fun reverseGet(b: B): A = reverseGet(b)
        }
    }
}






