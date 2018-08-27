package com.github.xiaofeidev.jvmimage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import xiaofei_dev.com.github.jvmimage.R

class MainActivity : AppCompatActivity() {

    companion object {
        val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101
        val REQUEST_SELECT_PICTURE = 0x01
    }

    private var mUri: Uri = Uri.parse("")

    private var mPressedTime: Long = 0

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        if (resultCode == android.app.Activity.RESULT_OK && requestCode == REQUEST_SELECT_PICTURE) {
            if (data != null) {
                mUri = data.data
                val path = getRealPathFromURI(mUri)
                startActivity<ImageAdjustActivity>(ImageAdjustActivity.IMAGE_PATH to path)
            }
        }
    }

    override fun onBackPressed() {
        val mNowTime = System.currentTimeMillis()//记录本次按键时刻
        if (mNowTime - mPressedTime > 1000) {//比较两次按键时间差
            toast("再按一次退出应用")
            mPressedTime = mNowTime
        } else {
            //退出程序
            super.onBackPressed()
        }
    }

    private fun initView(){
        toolBarMain.title = ""
        setSupportActionBar(toolBarMain)
        start.setOnClickListener {
            pickFromGallery()
        }
        btn_open_new.setOnClickListener {
            pickFromGallery()
        }
    }

    //权限请求回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_STORAGE_READ_ACCESS_PERMISSION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    //选择图片
    private fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION)
        } else {
            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            //这样会有一个自定义选择器
//            startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_image_hint)), REQUEST_SELECT_PICTURE);
            startActivityForResult(intent, REQUEST_SELECT_PICTURE)
        }
    }

    //获取图片的真实路径,很重要
    private fun getRealPathFromURI(contentURI: Uri): String {
        val result: String
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    //请求权限
    fun Activity.requestPermission(permissions: Array<String>,requestCode: Int){
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}
