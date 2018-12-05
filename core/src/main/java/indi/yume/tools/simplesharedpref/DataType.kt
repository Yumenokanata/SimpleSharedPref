package indi.yume.tools.simplesharedpref

import android.annotation.SuppressLint
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class BaseType<T>(val baseField: SharedField<T>)

object SharedString : BaseType<String?>(
    baseField = SharedField(
        writer = Reader { (ed, k, r) -> IO { ed.putString(k, r); Unit } },
        reader = Reader { (sp, k) -> IO { sp.getString(k, null) } }
    )
)

object SharedStringSet : BaseType<Set<String>?>(
    baseField = SharedField(
        writer = Reader { (ed, k, r) -> IO { ed.putStringSet(k, r); Unit } },
        reader = Reader { (sp, k) -> IO { sp.getStringSet(k, null) } }
    )
)

object SharedInt : BaseType<Int?>(
    baseField = SharedField(
        writer = Reader { (ed, k, r) -> IO { if (r != null) ed.putInt(k, r) else ed.remove(k); Unit } },
        reader = Reader { (sp, k) -> IO { if (sp.contains(k)) sp.getInt(k, 0) else null } }
    )
)

object SharedLong : BaseType<Long?>(
    baseField = SharedField(
        writer = Reader { (ed, k, r) -> IO { if (r != null) ed.putLong(k, r) else ed.remove(k); Unit } },
        reader = Reader { (sp, k) -> IO { if (sp.contains(k)) sp.getLong(k, 0) else null } }
    )
)

object SharedFloat : BaseType<Float?>(
    baseField = SharedField(
        writer = Reader { (ed, k, r) -> IO { if (r != null) ed.putFloat(k, r) else ed.remove(k); Unit } },
        reader = Reader { (sp, k) -> IO { if (sp.contains(k)) sp.getFloat(k, 0f) else null } }
    )
)

object SharedBoolean : BaseType<Boolean?>(
    baseField = SharedField(
        writer = Reader { (ed, k, r) -> IO { if (r != null) ed.putBoolean(k, r) else ed.remove(k); Unit } },
        reader = Reader { (sp, k) -> IO { if (sp.contains(k)) sp.getBoolean(k, false) else null } }
    )
)


typealias PrefKey = String

data class SharedField<T> internal constructor(
    internal val writer: Reader<ForIO, Triple<SharedPreferences.Editor, PrefKey, T>, Unit>,
    internal val reader: Reader<ForIO, Pair<SharedPreferences, PrefKey>, T>
) {
    companion object {
        operator fun <T> invoke(writer: (Triple<SharedPreferences.Editor, PrefKey, T>) -> Unit,
                                reader: (Pair<SharedPreferences, PrefKey>) -> T): SharedField<T> =
            SharedField(
                writer = Reader { triple -> IO { writer(triple) } },
                reader = Reader { pair -> IO { reader(pair) } }
            )
    }

    fun <R> map(wf: (R) -> T, rf: (T) -> R): SharedField<R> =
            SharedField(Reader { (ed, k, r) -> writer.run(Triple(ed, k, wf(r))) },
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
        val result = field.reader.run(Pair(prefModel.sharedPreference, key ?: property.name))
            .fix().attempt().unsafeRunSync()

        return when (result) {
            is Either.Left<Throwable> -> throw result.a
            is Either.Right<T> -> result.b
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val editor = prefModel.sharedPreference.edit()
        val result = field.writer.run(Triple(editor, key ?: property.name, value))
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






