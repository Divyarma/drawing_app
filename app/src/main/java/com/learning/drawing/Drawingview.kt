 package com.learning.drawing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class  Drawingview(context: Context,attrs :AttributeSet):View(context, attrs){

    private var mDrawPath : CustomPath? =null
    private var mCanvasBitmap:Bitmap?=null
    private var mDrawPaint:Paint?=null
    private var mCanvasPaint:Paint?=null
    private var mBrushSize:Float=0.toFloat()
    private var color = Color.BLACK
    private var canvas:Canvas?=null
    private val mPaths =ArrayList<CustomPath>()
    private val mUndoPaths=ArrayList<CustomPath>()

    init{
        setupDrawing()
    }

    fun onClickUndo(){
        if(mPaths.size>0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
            invalidate()
        }
    }

    private fun setupDrawing(){
        mDrawPaint= Paint()
        mDrawPath=CustomPath(color,mBrushSize)
        mDrawPaint!!.color=color
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.strokeJoin=Paint.Join.ROUND
        mDrawPaint!!.strokeCap=Paint.Cap.ROUND
        mCanvasPaint= Paint(Paint.DITHER_FLAG)
        mBrushSize=20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas= Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        for(path in mPaths){
            mDrawPaint!!.strokeWidth=path!!.brushthickness
            mDrawPaint!!.color=path!!.color
            canvas.drawPath(path!!, mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth=mDrawPath!!.brushthickness
            mDrawPaint!!.color=mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchx=event?.x
        val touchy=event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                mDrawPath!!.color=color
                mDrawPath!!.brushthickness=mBrushSize

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchx!!,touchy!!)
            }
            MotionEvent.ACTION_MOVE->{
                mDrawPath!!.lineTo(touchx!!,touchy!!)
            }
            MotionEvent.ACTION_UP->{
                mPaths.add(mDrawPath!!)
                mDrawPath=CustomPath(color,mBrushSize)
            }
            else->{return false}
        }
        invalidate()
        return true
        //return super.onTouchEvent(event)

    }

     fun SetSizeForBrush(newSize:Float){
        mBrushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth=mBrushSize
    }

     fun SetColourForBrush(colr:Int){
         color=colr
     }

    internal inner class CustomPath(var color:Int, var brushthickness:Float) : Path(){

    }


}