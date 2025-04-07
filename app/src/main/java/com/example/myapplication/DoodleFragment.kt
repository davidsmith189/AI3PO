package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentDoodleBinding

class DoodleFragment : Fragment() {

    private lateinit var binding: FragmentDoodleBinding
    private val colors = arrayOf(
        Color.BLACK,
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.YELLOW,
        Color.MAGENTA
    )
    private var currentColorIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDoodleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClear.setOnClickListener {
            binding.drawingView.clear()
        }

        binding.btnUndo.setOnClickListener {
            binding.drawingView.undo()
        }

        binding.btnColor.setOnClickListener {
            currentColorIndex = (currentColorIndex + 1) % colors.size
            binding.drawingView.setColor(colors[currentColorIndex])
        }

        binding.seekBarWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.drawingView.setStrokeWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
} 