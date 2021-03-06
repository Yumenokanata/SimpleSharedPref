package indi.yume.tools.simplesharedpref.sample

import android.content.Context
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import indi.yume.tools.simplesharedpref.Pipe
import indi.yume.tools.simplesharedpref.PrefModel
import indi.yume.tools.simplesharedpref.extensions.*
import indi.yume.tools.simplesharedpref.gson.byGson
import indi.yume.tools.simplesharedpref.gson.gsonNullablePref
import indi.yume.tools.simplesharedpref.moshi.byMoshi
import indi.yume.tools.simplesharedpref.moshi.moshiPref


val beanMapper: Pipe<String, TestBean> = createPipe(write = { it.note }, read = { TestBean(it) })

val beanListMapper: Pipe<List<String>, List<TestBean>> = mapList(beanMapper)

data class TestBean(val note: String)


object TestModel : PrefModel() {
    override val fileName: String = "test_shared_pref"

    override val prefMode: Int = Context.MODE_PRIVATE

    var string: String by stringPref("default").bind()

    var stringSet: Set<String> by stringSetPref(setOf<String>()).bind(key = "set_key")

    var intNum: Int by intPref(-1).bind(key = "int_key", commitWrite = false)

    var longNum: Long by longPref(-1L).bind()

    var floatNum: Float by floatPref(-1f).bind()

    var boolean: Boolean by booleanPref(true).bind()



    var list: List<TestBean> by stringSetNullablePref.pipe(
        nonNull(setOf<String>()) pipe list2set()
                pipe mapList(cipherAES("password"))
                pipe map<List<String>, List<TestBean>>(write = { bList -> bList.map { it.note } }, read = { sList -> sList.map { TestBean(it) } })
    ).bind()

    var list2: List<TestBean> by stringSetNullablePref.pipe(
        nonNull(setOf<String>()) pipe list2set() pipe beanListMapper
    ).bind()

    var list3: List<TestBean> by stringSetNullablePref
        .pipe(nonNull(setOf<String>()))
        .pipe(list2set())
        .pipe(beanListMapper)
        .bind()

    var cipherString: String? by stringNullablePref
        .pipeNullable(base64())
        .pipeNullable(cipherAES("password"))
        .bind()

    var cipherBean: List<TestBean>? by stringSetNullablePref
        .pipeNullable(set2Seq())
        .pipe(mapNullable(mapSeq(base64() pipe cipherAES("password"))))
        .pipeNullable(seq2List<String>() pipe beanListMapper)
        .bind()

    /**
     * Use moshi-pref need set PrefGlobeConfig.moshi first.
     */
    var moshiItem: TestBean by stringNullablePref
        .flatMapNullable(byMoshi<TestBean>(Moshi.Builder().build()))
        .pipe(nonNull(TestBean("empty")))
        .bind()

    var moshiItem2: TestBean by moshiPref(TestBean("empty")).bind()

    /**
     * Use gson-pref need set PrefGlobeConfig.gson first.
     */
    var gsonItem: TestBean by stringNullablePref
        .flatMapNullable(byGson<TestBean>(Gson()))
        .pipe(nonNull(TestBean("empty")))
        .bind()

    var gsonItem2: TestBean? by gsonNullablePref(TestBean::class.java).bind()
}

var outterField: TestBean? by stringNullablePref.pipeNullable(beanMapper).bindTo(TestModel)
