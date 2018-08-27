package com.github.xiaofeidev.jvmimagekit

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Created by xiaofei on 2017/12/3.
 * *desc:用于调整图像的亮度饱和度等参数值
 */

class JVMImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    //原始图像
    private var mBitmapOrigin: Bitmap? = null
    //待绘制图像
    private var mBitmapDraw: Bitmap? = null
    //屏幕尺寸
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    private val mColorMatrixFinal = ColorMatrix()
    private val mPaintColorMatrix = Paint()
    private val mCanvasColorMatrix = Canvas()

    private val mColorMatrixBrightness = ColorMatrix()
    private val mColorMatrixExposure = ColorMatrix()
    private val mColorMatrixSaturation = ColorMatrix()
    private val mColorMatrixContrast = ColorMatrix()
    private val mColorMatrixHue = ColorMatrix()

    init {
        //关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mPaintColorMatrix.isAntiAlias = true // 设置抗锯齿,也即是边缘做平滑处理
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //必须重写 onMeasure 里的逻辑以便 wrap_content 生效
        // 获取宽-测量规则的模式和大小
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        // 获取高-测量规则的模式和大小
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        // 设置wrap_content的默认宽 / 高值
        // 默认宽/高的设定并无固定依据,根据需要灵活设置
        // 类似TextView,ImageView等针对wrap_content均在onMeasure()对设置默认宽 / 高值有特殊处理,具体读者可以自行查看
        var mWidth = 0
        var mHeight = 0
        mBitmapOrigin?.let {
            mWidth = it.width
            mHeight = it.height
        }
        // 当模式是AT_MOST（即wrap_content）时设置默认值
        if (widthMode == View.MeasureSpec.AT_MOST && heightMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(mWidth, mHeight)
            // 宽 / 高任意一个模式为AT_MOST（即wrap_content）时，都设置默认值
        } else if (widthMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(mWidth, heightSize)
        } else if (heightMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, mHeight)
        }
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmapDraw = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        mCanvasColorMatrix.setBitmap(mBitmapDraw)

        if (mBitmapOrigin != null){
            applyColorMatrix()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (mBitmapDraw != null){
            canvas.drawBitmap(mBitmapDraw,0f,0f,null)
        }
    }

    //设置图片
    fun reSetBitmapOrigin(bitmap: Bitmap){
        mBitmapOrigin = bitmap
        requestLayout()
        applyColorMatrix()
    }

    //应用颜色矩阵变换
    private fun applyColorMatrix(){
        mColorMatrixFinal.reset()
        mColorMatrixFinal.postConcat(mColorMatrixBrightness)
        mColorMatrixFinal.postConcat(mColorMatrixExposure)
        mColorMatrixFinal.postConcat(mColorMatrixSaturation)
        mColorMatrixFinal.postConcat(mColorMatrixContrast)
        mColorMatrixFinal.postConcat(mColorMatrixHue)

        mCanvasColorMatrix.drawColor(Color.TRANSPARENT)
        mPaintColorMatrix.setColorFilter(ColorMatrixColorFilter(mColorMatrixFinal))// 设置颜色变换效果
        mCanvasColorMatrix.drawBitmap(mBitmapOrigin, 0f, 0f, mPaintColorMatrix)// 将颜色变化后的图片输出到新创建的位图区
        invalidate()
    }

    //调整亮度？
    fun transformColorMatrixBrightness(progress:Float){
        mColorMatrixBrightness.set(floatArrayOf(1f, 0f, 0f, 0f, progress,
                0f, 1f, 0f, 0f, progress,
                0f, 0f, 1f, 0f, progress,
                0f, 0f, 0f, 1f, 0f))
        applyColorMatrix()
    }

    //调整曝光度？
    fun transformColorMatrixExposure(progress:Float){
        mColorMatrixExposure.setScale(progress,progress,progress,1f)
        applyColorMatrix()
    }

    //调整饱和度
    fun transformColorMatrixSaturation(progress:Float){
        mColorMatrixSaturation.setSaturation(progress)
        applyColorMatrix()
    }

    //调整对比度
    fun transformColorMatrixContrast(progress:Float){
        val contrast: Float = progress
        val scale = (contrast / 255f - 0.5f) / -0.5f
        mColorMatrixContrast.set(floatArrayOf(scale, 0f, 0f, 0f, contrast,
                0f, scale, 0f, 0f, contrast,
                0f, 0f, scale, 0f, contrast,
                0f, 0f, 0f, 1f, 0f))
        applyColorMatrix()
    }

    //调整色阶
    fun transformColorMatrixHue(progress:Float){
        mColorMatrixHue.setRotate(0,progress)
        mColorMatrixHue.setRotate(1,progress)
        mColorMatrixHue.setRotate(2,progress)
        applyColorMatrix()
    }
}
