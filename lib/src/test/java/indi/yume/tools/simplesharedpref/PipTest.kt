package indi.yume.tools.simplesharedpref

import android.util.Base64
import indi.yume.tools.simplesharedpref.extensions.*
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import org.junit.Assert
import org.junit.Assert.assertEquals

class PipeTest {
    fun test() {
        forAll(Gen.string())
        { s ->
            val pip = nonNull("test")

            assertEquals(pip.get(s), s)
            assertEquals(pip.get(null), null)

            assertEquals(pip.reverseGet(s), s)

            true
        }
    }
}