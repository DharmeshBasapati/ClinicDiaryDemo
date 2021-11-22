package com.app.clinicdiarydemo.facecheck

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent

class DrawingView(context: Context?): View(context) {
    var mPaint: Paint? = null

    //MaskFilter  mEmboss;
    //MaskFilter  mBlur;
    var mBitmap: Bitmap? = null
    var mCanvas: Canvas? = null
    var mPath: Path? = null
    var mBitmapPaint: Paint? = null

    init {
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.isDither = true
        mPaint!!.color = -0x10000
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeJoin = Paint.Join.ROUND
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.strokeWidth = 15F
        mPath = Path()
        mBitmapPaint = Paint()
        mBitmapPaint!!.color = Color.RED
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
    }

    override fun draw(canvas: Canvas) {
        // TODO Auto-generated method stub
        super.draw(canvas)
        canvas.drawBitmap(mBitmap!!, 0F, 0F, mBitmapPaint)
        canvas.drawPath(mPath!!, mPaint!!)
    }

    private var mX = 0f
    private  var mY:kotlin.Float = 0f
    private val TOUCH_TOLERANCE = 4f

    private fun touch_start(x: Float, y: Float) {
        //mPath.reset();
        mPath?.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touch_move(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy: Float = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath?.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touch_up() {
        mPath?.lineTo(mX, mY)
        // commit the path to our offscreen
        mCanvas?.drawPath(mPath!!, mPaint!!)
        //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        // kill this so we don't double draw
        mPath!!.reset()
        // mPath= new Path();
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touch_start(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touch_move(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touch_up()
                invalidate()
            }
        }
        return true
    }
}