package com.hx.hongbao

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import com.hailian.qmyg.IMessage
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    private var message: IMessage? = null
    private val conn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            // TODO
            message = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            // TODO
            Log.e("AIDL=========", "onServiceConnected=========")
            message = IMessage.Stub.asInterface(p1)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val service = Intent("com.hailian.qmyg.service.MessageService")
//        service.`package` = "com.hailian.qmyg"
//        bindService(service, conn, Context.BIND_AUTO_CREATE)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if (message != null) {
//        }
//        unbindService(conn)
//    }

    fun startPlugin(view: View) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    fun getPermission(view: View) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                toast("您已经成功获取权限！")
            } else {
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + packageName)).let {
                    startActivity(it)
                }
            }
        } else {
            toast("获取权限成功！")
        }
    }
}
