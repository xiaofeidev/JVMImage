package com.github.xiaofeidev.jvmimage.extension

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.TypedValue
import android.view.View
import org.jetbrains.anko.toast
import xiaofei_dev.com.github.jvmimage.R
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.System
import kotlin.String

/**
 * Created by xiaofei on 2017/6/22.
 */

//给 Activity 扩展的方法，保存图片到 SD 卡指定名称的文件夹
fun Activity.saveImageToDir(bmp: Bitmap,dir:String):Boolean{
    var save = false
    val appDir = File(Environment.getExternalStorageDirectory(), dir)
    if(!appDir.exists()){
        appDir.mkdir()
    }
    val fileName:String = System.currentTimeMillis().toString() + ".png"
    val file = File(appDir, fileName)
    try {
        val fos = FileOutputStream(file)
        save = bmp.compress(Bitmap.CompressFormat.PNG,100,fos)
        fos.flush()
        fos.close()
    }catch (e: Exception){
        save = false
        e.printStackTrace()
        toast(e.toString())
    }
    if(save){
        //通知图库更新
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.path)))
        toast("图片已成功保存至\n ${file.path}")
    }else{
        toast(R.string.save_failure)
    }
    return save
}

//给 Activity 扩展的方法，获取普通 View 截图
fun Activity.getViewBitmap(v: View): Bitmap {
    v.isDrawingCacheEnabled = true
    v.buildDrawingCache(true)
    val bitmap = Bitmap.createBitmap(v.getDrawingCache(true), 0, 0, v.measuredWidth, v.measuredHeight)
    v.isDrawingCacheEnabled = false
    v.destroyDrawingCache()
    return bitmap
}


//Activity 的扩展属性，actionBarSize
val Activity.actionBarSize:Int
    get() {
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }else{
            return 0
        }
    }


