package indi.yume.tools.simplesharedpref

internal interface HK<out F, out A>
internal typealias HK2<F, A, B> = HK<HK<F, A>, B>
internal typealias HK3<F, A, B, C> = HK<HK<HK<F, A>, B>, C>

internal class Reader<F, D, A>(val run: (D) -> HK<F, A>) : ReaderOf<F, D, A> {
    internal fun <B> map(FF: Functor<F>, f: (A) -> B): Reader<F, D, B> = FF.run {
        Reader { a -> run(a).map { f(it) } }
    }
}

internal sealed class Either<out A, out B> {
    data class Left<out A>(val a: A) : Either<A, Nothing>()
    data class Right<out B>(val b: B) : Either<Nothing, B>()
}

internal class IO<A> internal constructor(internal val thunk: () -> A) : IOOf<A> {
    companion object {
        operator fun <A> invoke(f: () -> A): IO<A> = IO(thunk = f)
    }

    fun <B> map(f: (A) -> B): IO<B> = IO(thunk = { f(thunk()) })

    fun <B> flatMap(f: (A) -> IOOf<B>): IO<B> = IO(thunk = { f(thunk()).fix().unsafeRunSync() })

    internal fun attempt(): IO<Either<Throwable, A>> = IO(thunk = {
        try {
            Either.Right(thunk())
        } catch (e: Throwable) {
            Either.Left(e)
        }
    })

    internal fun runSync(cb: (Either<Throwable, A>) -> IOOf<Unit>): IO<Unit> =
        IO { cb(attempt().unsafeRunSync()).fix().unsafeRunSync() }

    fun unsafeRunSync(): A = thunk()
}

internal fun IO.Companion.functor(): Functor<ForIO> = IOFunctorInstance

internal object IOFunctorInstance : Functor<ForIO> {
    override fun <A, B> HK<ForIO, A>.map(f: (A) -> B): HK<ForIO, B> = fix().map(f)
}

internal interface Functor<F> {
    fun <A, B> HK<F, A>.map(f: (A) -> B): HK<F, B>
}

internal class ForReader private constructor() { companion object }
internal typealias ReaderOf<F, D, A> = HK3<ForReader, F, D, A>
internal typealias ReaderPartialOf<F, D> = HK2<ForReader, F, D>
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <F, D, A> ReaderOf<F, D, A>.fix(): Reader<F, D, A> =
    this as Reader<F, D, A>

internal class ForIO private constructor() { companion object }
internal typealias IOOf<A> = HK<ForIO, A>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <A> IOOf<A>.fix(): IO<A> =
    this as IO<A>
