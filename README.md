# SimpleSharedPref
  
一个简单的Android的SharedPreferences包装  
  
可以简单进行自定义
  
现包括:
  
1. 自由的自定义数据的序列化和反序列化
2. 强类型
3. Moshi和Gson的支持
4. 枚举类型的支持
5. 加密和base64默认支持
6. 可自定义对复杂类型的支持
  
---

Add this in your root build.gradle at the end of repositories:
```groovy
allprojects {
	repositories {
        jcenter()
		maven { url "https://jitpack.io" }
	}
}
```

Step 2. Add the dependency in your module's gradle file
```groovy
dependencies {
    implementation 'com.github.Yumenokanata.SimpleSharedPref:lib:master'
    implementation 'com.github.Yumenokanata.SimpleSharedPref:gson-pref:master' // optional
    implementation 'com.github.Yumenokanata.SimpleSharedPref:moshi-pref:master' // optional
}
```

---

Sample:
  
```kotlin
val beanMapper: Pipe<String, TestBean> = createPipe(write = { it.note }, read = { TestBean(it) })

val beanListMapper: Pipe<List<String>, List<TestBean>> = mapList(beanMapper)

data class TestBean(val note: String)


object TestModel : PrefModel() {
    // SharedPreferences文件名配置
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
        // 对数据取base64
        .pipeNullable(base64())
        .pipeNullable(cipherAES("password"))
        .bind()

    var cipherBean: List<TestBean>? by stringSetNullablePref
        .pipeNullable(set2Seq())
        // 采用AES对数据进行加密(AES默认会对数据base64化)
        .pipe(mapNullable(mapSeq(cipherAES("password"))))
        .pipeNullable(seq2List<String>() pipe beanListMapper)
        .bind()

    /**
     * Use moshi-pref need set PrefGlobeConfig.moshi first.
     */
    var moshiItem: TestBean by stringNullablePref
        .pipeNullable(byMoshi<TestBean>(Moshi.Builder().build()))
        .pipe(nonNull(TestBean("empty")))
        .bind()

    var moshiItem2: TestBean by moshiPref(TestBean("empty")).bind()

    /**
     * Use gson-pref need set PrefGlobeConfig.gson first.
     */
    var gsonItem: TestBean by stringNullablePref
        .pipeNullable(byGson<TestBean>(Gson()))
        .pipe(nonNull(TestBean("empty")))
        .bind()

    var gsonItem2: TestBean? by gsonNullablePref<TestBean>().bind()
}

var outterField: TestBean? by stringNullablePref.pipeNullable(beanMapper).bindTo(TestModel)

```

---
  
主要结构:

```
SharedField  --> Pipe1 --> Pipe2 --....-> PipeX --> bind() or bindTo()
```

`SharedField`: 基本的Field对象, 包括序列化到SharedPreferences和从SharedPreferences反序列化用的逻辑  
`Pipe`: 管道, 用于改变数据类型. 包括加密管道, 序列转换, base64管道  
`bind()`和`bindTo()`: 绑定到`PrefModel`(包括获取key, fileName等), 转换为实际进行代理的ReadWriteProperty对象
  
  
### License
<pre>
Copyright 2018 Yumenokanata

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
