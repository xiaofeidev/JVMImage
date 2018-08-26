package com.github.xiaofeidev.jvmimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.github.xiaofeidev.jvmimage.extension.SelectedStateListDrawable
import com.github.xiaofeidev.jvmimage.extension.getViewBitmap
import com.github.xiaofeidev.jvmimage.extension.saveImageToDir
import kotlinx.android.synthetic.main.activity_image_adjust.*
import xiaofei_dev.com.github.jvmimage.R
import org.jetbrains.anko.toast
import java.util.*

class ImageAdjustActivity : AppCompatActivity() {
    var mSelectedBtn: View? = null
    var mSelectedSeeakBarContainer: View? = null

    var mLastProgress : Int = 0
    //操作回退栈
    val mActionBackStack = Stack<ActionTrack>()
    //操作前进栈
    val mActionForwardStack = Stack<ActionTrack>()

    //Acitivity 自加载完毕后第一次获得焦点
    var isFirst:Boolean = true

    val mImagePath by lazy {
        intent.getStringExtra(IMAGE_PATH)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_adjust)
        init()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isFirst){
            //图片最终尺寸
            var width:Int
            var height:Int
            //待加载图像的实际尺寸
            var bmpWidth:Float
            var bmpHeight:Float
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(mImagePath,options)
            bmpWidth = options.outWidth.toFloat()
            bmpHeight = options.outHeight.toFloat()
            //这里需要调换测量出的图片宽高值，具体原因参考下面两个网址
            //https://my.oschina.net/u/1444935/blog/313191
            //https://developer.android.com/reference/android/media/ExifInterface.html#TAG_ORIENTATION
            val imgOri = ExifInterface(mImagePath).getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED)
            //如果图片的旋转值为 90°
            if (imgOri == ExifInterface.ORIENTATION_ROTATE_90 || imgOri == ExifInterface.ORIENTATION_ROTATE_270){
                val m = bmpWidth
                bmpWidth = bmpHeight
                bmpHeight = m
            }
            //这一堆判断看来挺乱，其实就是把待加载图片的最终尺寸设为充满容器且没有白边且适应屏幕
            if (bmpWidth == bmpHeight){
                width = mAdjustImageContainer.measuredWidth
                height = width
            }else if(bmpWidth > bmpHeight){
                width = mAdjustImageContainer.measuredWidth
                height = (width.toFloat()/bmpWidth * bmpHeight).toInt()
            }else{
                height = mAdjustImageContainer.measuredHeight
                width = (height.toFloat()/bmpHeight * bmpWidth).toInt()
                if(width > mAdjustImageContainer.measuredWidth){
                    val oldWidth = width
                    width = mAdjustImageContainer.measuredWidth
                    height = (width.toFloat()/oldWidth * height).toInt()
                }
            }

            val glideOptions: RequestOptions = RequestOptions()
                    .error(R.drawable.pic_default)
                    .fitCenter()
            Glide.with(this).asBitmap().load(mImagePath).apply(glideOptions).into(object : SimpleTarget<Bitmap>(width, height) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    mAdjustImageView.reSetBitmapOrigin(resource)
                }
            })
            isFirst = false
        }
    }

    private fun init(){
        btnBack.setOnClickListener {
            onBackPressed()
        }
        btnDone.setOnClickListener {
            toast(R.string.processing)
            saveImageToDir(getViewBitmap(mAdjustImageView), "JVMIMage")
        }
        imageBrightness.setImageDrawable(SelectedStateListDrawable(imageBrightness.drawable,Color.WHITE))
        imageContrast.setImageDrawable(SelectedStateListDrawable(imageContrast.drawable,Color.WHITE))
        imageExposure.setImageDrawable(SelectedStateListDrawable(imageExposure.drawable,Color.WHITE))
        imageSaturation.setImageDrawable(SelectedStateListDrawable(imageSaturation.drawable,Color.WHITE))
        imageHue.setImageDrawable(SelectedStateListDrawable(imageHue.drawable,Color.WHITE))
        mSelectedBtn = btnBrightness
        mSelectedBtn?.isSelected = true
        mSelectedSeeakBarContainer = mSeekBarBrightnessContainer
        btnBrightness.setOnClickListener {v ->
            if(mSelectedBtn != v){
                mSelectedBtn?.isSelected = false
                mSelectedBtn = v
                mSelectedBtn?.isSelected = true
                mSelectedSeeakBarContainer?.visibility = View.GONE
                mSelectedSeeakBarContainer = mSeekBarBrightnessContainer
                mSelectedSeeakBarContainer?.visibility = View.VISIBLE
            }
        }
        btnContrast.setOnClickListener { v ->
            if(mSelectedBtn != v){
                mSelectedBtn?.isSelected = false
                mSelectedBtn = v
                mSelectedBtn?.isSelected = true
                mSelectedSeeakBarContainer?.visibility = View.GONE
                mSelectedSeeakBarContainer = mSeekBarContrastContainer
                mSelectedSeeakBarContainer?.visibility = View.VISIBLE
            }
        }
        btnSaturation.setOnClickListener { v ->
            if(mSelectedBtn != v){
                mSelectedBtn?.isSelected = false
                mSelectedBtn = v
                mSelectedBtn?.isSelected = true
                mSelectedSeeakBarContainer?.visibility = View.GONE
                mSelectedSeeakBarContainer = mSeekBarSaturationContainer
                mSelectedSeeakBarContainer?.visibility = View.VISIBLE
            }
        }
        btnExposure.setOnClickListener { v ->
            if(mSelectedBtn != v){
                mSelectedBtn?.isSelected = false
                mSelectedBtn = v
                mSelectedBtn?.isSelected = true
                mSelectedSeeakBarContainer?.visibility = View.GONE
                mSelectedSeeakBarContainer = mSeekBarExposureContainer
                mSelectedSeeakBarContainer?.visibility = View.VISIBLE
            }
        }
        btnHue.setOnClickListener { v ->
            if(mSelectedBtn != v){
                mSelectedBtn?.isSelected = false
                mSelectedBtn = v
                mSelectedBtn?.isSelected = true
                mSelectedSeeakBarContainer?.visibility = View.GONE
                mSelectedSeeakBarContainer = mSeekBarHueContainer
                mSelectedSeeakBarContainer?.visibility = View.VISIBLE
            }
        }

        mSeekBarBrightness.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var length = progress.toFloat()
                length = (length - 50)/50 * 255
                mAdjustImageView.transformColorMatrixBrightness(length)
                textBrightness.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mLastProgress = seekBar!!.progress
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mActionBackStack.add(ActionTrack(ADJUST_BRIGHTNESS,seekBar?.progress?.minus(mLastProgress)))
                //如有新的操作加入回退栈则清空前进栈！！！
                if (mActionForwardStack.isNotEmpty()){
                    mActionForwardStack.clear()
                }
            }
        })

        mSeekBarContrast.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var length = progress.toFloat()
                length = (50 - length)/50 * 255
                mAdjustImageView.transformColorMatrixContrast(length)
                textContrast.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mLastProgress = seekBar!!.progress
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mActionBackStack.add(ActionTrack(ADJUST_CONTRAST,seekBar?.progress?.minus(mLastProgress)))
                //如有新的操作加入回退栈则清空前进栈！！！
                if (mActionForwardStack.isNotEmpty()){
                    mActionForwardStack.clear()
                }
            }
        })

        mSeekBarSaturation.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var length = progress.toFloat()
                if (length < 50){
                    length = length/50
                }else if(length > 50){
                    length = (length - 50)/10 + 1
                }else{
                    length = 1f
                }
                mAdjustImageView.transformColorMatrixSaturation(length)
                textSaturation.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mLastProgress = seekBar!!.progress
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mActionBackStack.add(ActionTrack(ADJUST_SATURATION,seekBar?.progress?.minus(mLastProgress)))
                //如有新的操作加入回退栈则清空前进栈！！！
                if (mActionForwardStack.isNotEmpty()){
                    mActionForwardStack.clear()
                }
            }
        })

        mSeekBarExposure.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var length = progress.toFloat()
                if (length < 50){
                    length = length/50
                }else if(length > 50){
                    length = (length - 50)/25 + 1
                }else{
                    length = 1f
                }
                mAdjustImageView.transformColorMatrixExposure(length)
                textExposure.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mLastProgress = seekBar!!.progress
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mActionBackStack.add(ActionTrack(ADJUST_EXPOSURE,seekBar?.progress?.minus(mLastProgress)))
                //如有新的操作加入回退栈则清空前进栈！！！
                if (mActionForwardStack.isNotEmpty()){
                    mActionForwardStack.clear()
                }
            }
        })

        mSeekBarHue.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var length = progress.toFloat()
                length = (length - 50)/50 * 180
                mAdjustImageView.transformColorMatrixHue(length)
                textHue.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mLastProgress = seekBar!!.progress
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mActionBackStack.add(ActionTrack(ADJUST_HUE,seekBar?.progress?.minus(mLastProgress)))
                //如有新的操作加入回退栈则清空前进栈！！！
                if (mActionForwardStack.isNotEmpty()){
                    mActionForwardStack.clear()
                }
            }
        })

        btnReset.setOnClickListener {
            when(mSelectedSeeakBarContainer?.id){
                R.id.mSeekBarBrightnessContainer ->{
                    val length = 50 - mSeekBarBrightness.progress
                    mSeekBarBrightness.progress = 50
                    if (length != 0){
                        mActionBackStack.add(ActionTrack(ADJUST_BRIGHTNESS,length))
                        //如有新的操作加入回退栈则清空前进栈！！！
                        if (mActionForwardStack.isNotEmpty()){
                            mActionForwardStack.clear()
                        }
                    }
                }
                R.id.mSeekBarContrastContainer ->{
                    val length = 50 - mSeekBarContrast.progress
                    mSeekBarContrast.progress = 50
                    if (length != 0){
                        mActionBackStack.add(ActionTrack(ADJUST_CONTRAST,length))
                        //如有新的操作加入回退栈则清空前进栈！！！
                        if (mActionForwardStack.isNotEmpty()){
                            mActionForwardStack.clear()
                        }
                    }
                }
                R.id.mSeekBarSaturationContainer ->{
                    val length = 50 - mSeekBarSaturation.progress
                    mSeekBarSaturation.progress = 50
                    if (length != 0){
                        mActionBackStack.add(ActionTrack(ADJUST_SATURATION,length))
                        //如有新的操作加入回退栈则清空前进栈！！！
                        if (mActionForwardStack.isNotEmpty()){
                            mActionForwardStack.clear()
                        }
                    }
                }
                R.id.mSeekBarExposureContainer ->{
                    val length = 50 - mSeekBarExposure.progress
                    mSeekBarExposure.progress = 50
                    if (length != 0){
                        mActionBackStack.add(ActionTrack(ADJUST_EXPOSURE,length))
                        //如有新的操作加入回退栈则清空前进栈！！！
                        if (mActionForwardStack.isNotEmpty()){
                            mActionForwardStack.clear()
                        }
                    }
                }
                R.id.mSeekBarHueContainer ->{
                    val length = 50 - mSeekBarHue.progress
                    mSeekBarHue.progress = 50
                    if (length != 0){
                        mActionBackStack.add(ActionTrack(ADJUST_HUE,length))
                        //如有新的操作加入回退栈则清空前进栈！！！
                        if (mActionForwardStack.isNotEmpty()){
                            mActionForwardStack.clear()
                        }
                    }
                }
            }
        }

        btnLast.setOnClickListener {
            if (mActionBackStack.isNotEmpty()){
                val action = mActionBackStack.pop()
                when(action.flag){
                    ADJUST_BRIGHTNESS ->{
                        mSeekBarBrightness.progress = mSeekBarBrightness.progress - action.length!!
                    }
                    ADJUST_CONTRAST ->{
                        mSeekBarContrast.progress = mSeekBarContrast.progress - action.length!!
                    }
                    ADJUST_SATURATION ->{
                        mSeekBarSaturation.progress = mSeekBarSaturation.progress - action.length!!
                    }
                    ADJUST_EXPOSURE ->{
                        mSeekBarExposure.progress = mSeekBarExposure.progress - action.length!!
                    }
                    ADJUST_HUE ->{
                        mSeekBarHue.progress = mSeekBarHue.progress - action.length!!
                    }
                }
                mActionForwardStack.push(action)
            }
        }

        btnNext.setOnClickListener {
            if (mActionForwardStack.isNotEmpty()){
                val action = mActionForwardStack.pop()
                when(action.flag){
                    ADJUST_BRIGHTNESS ->{
                        mSeekBarBrightness.progress = mSeekBarBrightness.progress + action.length!!
                    }
                    ADJUST_CONTRAST ->{
                        mSeekBarContrast.progress = mSeekBarContrast.progress + action.length!!
                    }
                    ADJUST_SATURATION ->{
                        mSeekBarSaturation.progress = mSeekBarSaturation.progress + action.length!!
                    }
                    ADJUST_EXPOSURE ->{
                        mSeekBarExposure.progress = mSeekBarExposure.progress + action.length!!
                    }
                    ADJUST_HUE ->{
                        mSeekBarHue.progress = mSeekBarHue.progress + action.length!!
                    }
                }
                mActionBackStack.push(action)
            }
        }
    }
    data class ActionTrack(val flag : Int, val length : Int? = 0)

    companion object {
        val ADJUST_BRIGHTNESS = 0
        val ADJUST_CONTRAST = 1
        val ADJUST_SATURATION = 2
        val ADJUST_EXPOSURE = 3
        val ADJUST_HUE = 6

        val IMAGE_PATH = "IMAGE_PATH"
    }
}
