package com.example.zhang.a3c_car

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : Activity(){

    private var host="192.168.0.1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermission()

        //激活按钮
        FlipButton.setOnTouchListener {
            view, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {

                }
                MotionEvent.ACTION_UP -> {
                    var intent = Intent(this,FlipActivity::class.java)
                    startActivity(intent)
                }
                else -> return@setOnTouchListener false
            }
            return@setOnTouchListener true
        }
        ServerButton.setOnTouchListener {
            view, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {

                }
                MotionEvent.ACTION_UP -> {
                    val intent = Intent(this@MainActivity, ServerActivity::class.java)
                    startActivityForResult(intent, 10)
                }
                else -> return@setOnTouchListener false
            }
            return@setOnTouchListener true
        }
        ClientButton.setOnTouchListener {
            view, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {

                }
                MotionEvent.ACTION_UP -> {
                    val intent = Intent(this,ClientActivity::class.java) //将ip地址传递给图像回传模块
                    intent.putExtra("a", host)
                    startActivity(intent)
                }
                else -> return@setOnTouchListener false
            }
            return@setOnTouchListener true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == 9) {
            super.onActivityResult(requestCode, resultCode, data)
            host = data.getStringExtra("a")
        }
    }

    private fun initPermission() {
        val permissions = arrayOf<String>(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        )

        val toApplyList = ArrayList<String>()

        for (perm in permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm)
                //进入到这里代表没有权限.
            }
        }
        val tmpList = arrayOfNulls<String>(toApplyList.size)
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123)
        }
    }
}

