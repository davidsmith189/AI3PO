package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

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
    
    fun saveToFile(): Uri? {
        if (paths.isEmpty() && !path.hasPoints()) {
            return null // Nothing to save
        }
        
        // Create a bitmap with the same dimensions as the view
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE) // Set background color
        
        // Draw all paths on the canvas
        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }
        canvas.drawPath(path, paint)
        
        try {
            // Create a file to save the bitmap
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Doodle_$timeStamp.jpg"
            val storageDir = context.getExternalFilesDir("Doodles")
            if (!storageDir?.exists()!!) {
                storageDir.mkdirs()
            }
            
            val file = File(storageDir, fileName)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
            
            // Return the content URI using FileProvider
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Helper method to check if a path is empty
    private fun Path.hasPoints(): Boolean {
        val pm = PathMeasure(this, false)
        return pm.length > 0
    }
} 