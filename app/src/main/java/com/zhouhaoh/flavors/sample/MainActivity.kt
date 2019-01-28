package com.zhouhaoh.flavors.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by zhouhaoh on 2019/01/20.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_env.text = "${getString(R.string.envTag)}\n包名:${BuildConfig.APPLICATION_ID}\nBaseUrl:${BuildConfig.BASE_URL}"
    }
}
