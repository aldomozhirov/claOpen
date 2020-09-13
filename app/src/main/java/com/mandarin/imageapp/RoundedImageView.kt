package com.mandarin.imageapp

import android.R.attr
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat


class RoundedImageView : AppCompatImageView {

    private val defaultCircleColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val defaultTextColor = Color.WHITE
    var annotation: String = ""
        set(value) {
            field = "AS"
            invalidate()
        }
    private var textSizeRatio = 0.5f

    private val textLabelBgPaint = Paint().apply {
        color = defaultCircleColor
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }
    private val bitmapPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }
    private val textPaint = Paint().apply {
        color = defaultTextColor
        isAntiAlias = true
        isDither = true
        textAlign = Paint.Align.CENTER
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttributes(context, attrs)
    }

    private fun initAttributes(context: Context?, attrs: AttributeSet?) {
        val attributes = context?.obtainStyledAttributes(attrs, R.styleable.RoundedImageView)
        try {
            attributes?.getColor(R.styleable.RoundedImageView_circleColor, defaultCircleColor)?.let {
                textLabelBgPaint.color = it
            }
            attributes?.getColor(R.styleable.RoundedImageView_android_textColor, defaultTextColor)?.let {
                textPaint.color = it
            }
        } finally {
            attributes?.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return
        val diameter = Math.min(width, height)
        when (drawable) {
            is VectorDrawable, is VectorDrawableCompat -> drawable.draw(canvas)
            is BitmapDrawable -> {
                drawBitmap(drawable = drawable as BitmapDrawable, canvas = canvas,
                    diameter = diameter)
            }
            else -> drawTextLabel(canvas = canvas, diameter = diameter)
        }
    }

    private fun drawBitmap(drawable: BitmapDrawable, canvas: Canvas, diameter: Int) {
        val initialBitmap = drawable.bitmap
        if (initialBitmap == null) {
            drawTextLabel(canvas, diameter)
        } else {
            val circleBitmap = initialBitmap.toCircleBitmap(diameter)
            canvas.drawBitmap(circleBitmap, (width - circleBitmap.width) / 2f,
                (height - circleBitmap.height) / 2f, bitmapPaint)
        }
    }

    private fun drawTextLabel(canvas: Canvas, diameter: Int) {
        val bitmap = emptyCircle(diameter, textLabelBgPaint)
        val textSize = diameter.toFloat() * textSizeRatio
        textPaint.textSize = textSize
        canvas.drawBitmap(bitmap, (width - bitmap.width) / 2f, (height - bitmap.height) / 2f, textLabelBgPaint)
        canvas.drawText(annotation, diameter / 2f, diameter * 0.5f + textSize * 0.33f, textPaint)
    }

    private fun Bitmap.toCircleBitmap(diameter: Int): Bitmap =
        emptyCircle(diameter, bitmapPaint).also {
            bitmapPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val rect = Rect(0, 0, diameter, diameter)
            Canvas(it).drawBitmap(scaleAndCropBitmap(this, diameter.toFloat()), rect, rect, bitmapPaint)
            bitmapPaint.xfermode = null
        }

    private fun emptyCircle(diameter: Int, paint: Paint): Bitmap =
        Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888).also {
//            Canvas(it).drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, paint)
            val rectangle = Rect(0, 0, diameter, diameter)
            Canvas(it).drawRect(rectangle, paint)
        }

    private fun scaleAndCropBitmap(bmp: Bitmap, fitSize: Float): Bitmap {
        val scaleFactor = Math.min(bmp.width, bmp.height) / fitSize
        val scaledBitmap = Bitmap.createScaledBitmap(bmp, ((bmp.width / scaleFactor) + 0.5).toInt(),
            ((bmp.height / scaleFactor) + 0.5).toInt(), false)
        return Bitmap.createBitmap(scaledBitmap,
            ((scaledBitmap.width - fitSize) / 2).toInt(),
            ((scaledBitmap.height - fitSize) / 2).toInt(),
            fitSize.toInt(), fitSize.toInt())
    }
}