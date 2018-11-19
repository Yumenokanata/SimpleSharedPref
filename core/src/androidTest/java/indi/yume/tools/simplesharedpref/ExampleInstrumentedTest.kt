package indi.yume.tools.simplesharedpref

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import indi.yume.tools.simplesharedpref.extensions.*
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        forAll(Gen.string())
        { s ->
            val pip = base64()
            val base64S = Base64.encodeToString(s.toByteArray(), Base64.NO_WRAP)

            assertEquals(pip.get(base64S), s)
            assertEquals(pip.reverseGet(s), base64S)

            true
        }

        forAll(Gen.list(Gen.int()))
        { list ->
            val pip: Pipe<List<String>, List<Int>> =
                mapList(createPipe(write = { r -> r.toString() }, read = { w -> w.toInt() }))

            val result = list.map { it.toString() }

            assertEquals(pip.get(result), list)
            assertEquals(pip.reverseGet(list), result)

            true
        }

        forAll(Gen.string())
        { s ->
            val pip = cipherAES("password")
            val result = pip.reverseGet(s)

            assertEquals(pip.get(result), s)
            assertEquals(pip.reverseGet(s), result)
            assertNotEquals(result, s)

            true
        }

    }
}
