package indi.yume.tools.simplesharedpref

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TestModel.string = "test"
        val set = TestModel.stringSet

        TestModel.gsonItem = TestBean("note")
        TestModel.gsonItem2 = null
    }
}
