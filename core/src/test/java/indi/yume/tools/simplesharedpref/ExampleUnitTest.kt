package indi.yume.tools.simplesharedpref

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
//        assertEquals(4, 2 + 2)

        fun test1(s: () -> String) = println("test1")

        test1(::s::get)
    }
}

val s: String by lazy { println("lazy s."); "sss" }
