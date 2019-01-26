package indi.yume.tools.simplesharedpref.extensions

import android.util.Base64
import indi.yume.tools.simplesharedpref.*
import java.nio.charset.Charset


val stringNullablePref: SharedField<String?> = SharedString.baseField

val stringSetNullablePref: SharedField<Set<String>?> = SharedStringSet.baseField

val intNullablePref: SharedField<Int?> = SharedInt.baseField

val longNullablePref: SharedField<Long?> = SharedLong.baseField

val floatNullablePref: SharedField<Float?> = SharedFloat.baseField

val booleanNullablePref: SharedField<Boolean?> = SharedBoolean.baseField


fun stringPref(defValue: String): SharedField<String> = stringNullablePref.pipe(nonNull(defValue))

fun stringSetPref(defValue: Set<String>): SharedField<Set<String>> = stringSetNullablePref.pipe(nonNull(defValue))

fun intPref(defValue: Int): SharedField<Int> = intNullablePref.pipe(nonNull(defValue))

fun longPref(defValue: Long): SharedField<Long> = longNullablePref.pipe(nonNull(defValue))

fun floatPref(defValue: Float): SharedField<Float> = floatNullablePref.pipe(nonNull(defValue))

fun booleanPref(defValue: Boolean): SharedField<Boolean> = booleanNullablePref.pipe(nonNull(defValue))


inline fun <reified T : Enum<T>> fromEnum(): Pipe<String, T> =
    fromEnum(T::class.java)

fun <T : Enum<T>> fromEnum(enumClass: Class<T>): Pipe<String, T> =
    createPipe(write = { r -> r.name },
        read = { w -> enumClass.enumConstants.first { it.name == w } })


fun <W : Any, R : Any> SharedField<W?>.pipeNullable(pip: Pipe<W, R>): SharedField<R?> =
    this.pipe(mapNullable(pip))

fun <W : Any, R : Any> SharedField<W?>.flatMapNullable(pip: Pipe<W, R?>): SharedField<R?> =
    this.pipe(flatmapNullable(pip))

infix fun <W, C, R> Pipe<W, C>.pipe(process: Pipe<C, R>): Pipe<W, R> =
    Pipe(get = { w -> process.get(get(w)) },
        reverseGet = { r -> reverseGet(process.reverseGet(r)) })

fun <W, R> createPipe(write: (R) -> W, read: (W) -> R): Pipe<W, R> =
    map(write, read)


fun <T> nonNull(defaultValue: T): Pipe<T?, T> =
    Pipe(get = { w -> w ?: defaultValue },
        reverseGet = { r -> r })

fun <T> list2set(): Pipe<Set<T>, List<T>> =
    Pipe(get = { w -> w.toList() },
        reverseGet = { r -> r.toSet() })

fun <T> saveObject(write: (T) -> String, read: (String) -> T): Pipe<String, T> =
    map(write, read)

fun <W, R> map(write: (R) -> W, read: (W) -> R): Pipe<W, R> =
    Pipe(get = read, reverseGet = write)

fun <W : Any, R : Any> mapNullable(pip: Pipe<W, R>): Pipe<W?, R?> =
    map(write = { r -> if (r != null) pip.reverseGet(r) else null },
        read = { w -> if (w != null) pip.get(w) else null })

fun <W : Any, R : Any> flatmapNullable(pip: Pipe<W, R?>): Pipe<W?, R?> =
    map(write = { r -> if (r != null) pip.reverseGet(r) else null },
        read = { w -> if (w != null) pip.get(w) else null })


fun <IT : Iterable<T>, T> fromSeq(f: (Sequence<T>) -> IT): Pipe<IT, Sequence<T>> =
    Pipe(get = { w -> w.asSequence() },
        reverseGet = { r -> f(r) })

fun <T, IT : Iterable<T>> toSeq(f: (Sequence<T>) -> IT): Pipe<Sequence<T>, IT> =
    Pipe(get = { w -> f(w) },
        reverseGet = { r -> r.asSequence() })

fun <W, R> mapSeq(write: (R) -> W, read: (W) -> R): Pipe<Sequence<W>, Sequence<R>> =
    Pipe(get = { w -> w.map { read(it) } },
        reverseGet = { r -> r.map { write(it) } })

fun <W, R> mapSeq(pip: Pipe<W, R>): Pipe<Sequence<W>, Sequence<R>> =
    mapSeq(write = pip::reverseGet, read = pip::get)


fun <T> list2Seq(): Pipe<List<T>, Sequence<T>> = fromSeq { it.toList() }

fun <T> seq2List(): Pipe<Sequence<T>, List<T>> = toSeq { it.toList() }

fun <T> set2Seq(): Pipe<Set<T>, Sequence<T>> = fromSeq { it.toSet() }

fun <T> seq2Set(): Pipe<Sequence<T>, Set<T>> = toSeq { it.toSet() }


fun <W, R> mapList(write: (R) -> W, read: (W) -> R): Pipe<List<W>, List<R>> =
    Pipe(get = { w -> w.map { read(it) } },
        reverseGet = { r -> r.map { write(it) } })

fun <W, R> mapList(pip: Pipe<W, R>): Pipe<List<W>, List<R>> =
    mapList(write = pip::reverseGet, read = pip::get)

fun <W, R> mapSet(write: (R) -> W, read: (W) -> R): Pipe<Set<W>, Set<R>> =
    Pipe(get = { w -> w.map { read(it) }.toSet() },
        reverseGet = { r -> r.map { write(it) }.toSet() })

fun <W, R> mapSet(pip: Pipe<W, R>): Pipe<Set<W>, Set<R>> =
    mapSet(write = pip::reverseGet, read = pip::get)


fun base64(flags: Int = Base64.NO_WRAP, charset: Charset = Charsets.UTF_8): Pipe<String, String> =
    Pipe(get = { w -> String(Base64.decode(w, flags), charset) },
        reverseGet = { r -> Base64.encodeToString(r.toByteArray(charset), flags) })


