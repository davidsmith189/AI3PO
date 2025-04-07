package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var path = Path()
    private var paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private var paths = mutableListOf<Pair<Path, Paint>>()
    private var currentX = 0f
    private var currentY = 0f

    init {
        setBackgroundColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        currentX = event.x
        currentY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(currentX, currentY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(currentX, currentY)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                paths.add(Pair(path, Paint(paint)))
                path = Path()
                return true
            }
        }
        return false
    }

    fun clear() {
        paths.clear()
        path.reset()
        invalidate()
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
    }

    fun setColor(color: Int) {
        paint.color = color
    }

    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
    }
} 