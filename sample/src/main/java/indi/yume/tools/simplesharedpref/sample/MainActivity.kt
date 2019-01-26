package indi.yume.tools.simplesharedpref.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TestModel.string = "test"
        val set = TestModel.stringSet
        TestModel.list = (1..1000).map { TestBean("note$it") }
        println(TestModel.list.joinToString())

        TestModel.gsonItem = TestBean("note")
        TestModel.gsonItem2 = null
        TestModel.moshiItem = TestBean("note")
        println(TestModel.moshiItem2)
    }
}
